package rdhxb.summaryservice.preprocessing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import rdhxb.common.summary.SummaryModel;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Turns the report JSON into compact, plain text that a small LLM can read easily:
 * one section per source, "Label: value" lines, no nulls / empty values / technical fields.
 */
@Component
@RequiredArgsConstructor
public class RaportPreprocessor {

    // Long lists (PKD, SUDOP aids, accounts) are cut to keep the prompt short
    private static final int MAX_LIST_ITEMS = 10;
    private static final String INDENT = "  ";

    private static final Pattern DATE_TIME = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T.*");
    private static final Pattern ENUM_CONSTANT = Pattern.compile("^[A-Z]+(_[A-Z]+)+$");
    private static final Pattern CAMEL_CASE_BOUNDARY = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])");

    // Technical or sensitive fields that do not help the model
    private static final Set<String> SKIPPED_FIELDS = Set.of(
            "fetchedAt", "requestDateTime", "wlRequestId", "identyfikatorWniosku",
            "ceidgId", "numerStatusu", "pesel"
    );

    private static final Map<String, String> SOURCE_NAMES = Map.of(
            "KRS", "KRS - Krajowy Rejestr Sądowy",
            "WL", "Biała lista podatników VAT",
            "CRBR", "CRBR - Centralny Rejestr Beneficjentów Rzeczywistych",
            "CEIDG", "CEIDG - Centralna Ewidencja Działalności Gospodarczej",
            "SUDOP", "SUDOP - otrzymana pomoc publiczna"
    );

    // Labels for fields whose camelCase name is unclear or English (WL)
    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("nip", "NIP"),
            Map.entry("regon", "REGON"),
            Map.entry("krs", "KRS"),
            Map.entry("krsNumber", "Numer KRS"),
            Map.entry("name", "Nazwa"),
            Map.entry("statusVat", "Status VAT"),
            Map.entry("statusOpp", "Status OPP"),
            Map.entry("residenceAddress", "Adres siedziby"),
            Map.entry("workingAddress", "Adres prowadzenia działalności"),
            Map.entry("registrationLegalDate", "Data rejestracji jako podatnik VAT"),
            Map.entry("registrationDenialDate", "Data odmowy rejestracji VAT"),
            Map.entry("registrationDenialBasis", "Podstawa odmowy rejestracji VAT"),
            Map.entry("restorationDate", "Data przywrócenia do rejestru VAT"),
            Map.entry("restorationBasis", "Podstawa przywrócenia do rejestru VAT"),
            Map.entry("removalDate", "Data wykreślenia z rejestru VAT"),
            Map.entry("removalBasis", "Podstawa wykreślenia z rejestru VAT"),
            Map.entry("accountNumbers", "Rachunki bankowe"),
            Map.entry("hasVirtualAccounts", "Posiada rachunki wirtualne"),
            Map.entry("kapitalZakladowy", "Kapitał zakładowy"),
            Map.entry("sklad", "Skład organu reprezentacji"),
            Map.entry("przedmiotPrzewazajacejDzialalnosci", "Przeważająca działalność (PKD)"),
            Map.entry("przedmiotPozostalejDzialalnosci", "Pozostała działalność (PKD)"),
            Map.entry("wzmiankaOZlozeniuRocznegoSprawozdaniaFinansowego", "Złożone sprawozdania finansowe"),
            Map.entry("zaOkresOdDo", "Za okres"),
            Map.entry("pkdGlowny", "Główne PKD"),
            Map.entry("powiazania", "Beneficjenci rzeczywiści"),
            Map.entry("wartoscNominalnaPln", "Wartość nominalna (PLN)"),
            Map.entry("wartoscBruttoPln", "Wartość brutto (PLN)"),
            Map.entry("wartoscBruttoEur", "Wartość brutto (EUR)")
    );

    private final ObjectMapper mapper;

    public String raportProcess(SummaryModel summaryModel) {
        JsonNode root = mapper.readTree(summaryModel.getPayload());
        StringBuilder sb = new StringBuilder();

        sb.append("=== DANE PODSTAWOWE ===\n");
        appendScalar(sb, 0, "NIP", root.path("nipNumber"));
        appendScalar(sb, 0, "Numer KRS", root.path("krsNumber"));
        appendScalar(sb, 0, "Data wygenerowania raportu", root.path("completedAt"));

        for (JsonNode source : root.path("raportSources")) {
            appendSource(sb, source);
        }
        return sb.toString().trim();
    }

    private void appendSource(StringBuilder sb, JsonNode source) {
        String code = source.path("source").asString("");
        sb.append("\n=== ").append(SOURCE_NAMES.getOrDefault(code, code)).append(" ===\n");

        switch (source.path("status").asString("")) {
            case "SUCCESS" -> {
                JsonNode payload = source.path("payload");
                if (isEmptyValue(payload)) {
                    sb.append("Brak wpisów.\n");
                } else if (payload.isArray()) {
                    appendArray(sb, 0, null, payload);
                } else {
                    appendObject(sb, 0, payload);
                }
            }
            case "SKIPPED" -> sb.append("Podmiot nie występuje w tym rejestrze.\n");
            case "FAILED" -> sb.append("Nie udało się pobrać danych z tego źródła.\n");
            default -> sb.append("Dane z tego źródła są niedostępne.\n");
        }
    }

    private void appendObject(StringBuilder sb, int depth, JsonNode object) {
        for (Map.Entry<String, JsonNode> field : object.properties()) {
            String key = field.getKey();
            JsonNode value = field.getValue();
            if (SKIPPED_FIELDS.contains(key) || isEmptyValue(value)) {
                continue;
            }

            String label = label(key);
            if (value.isArray()) {
                appendArray(sb, depth, label, value);
            } else if (value.isObject()) {
                if (isFlat(value)) {
                    // small nested object (e.g. pkdGlowny) fits in one line
                    line(sb, depth).append(label).append(": ").append(inline(value)).append('\n');
                } else {
                    line(sb, depth).append(label).append(":\n");
                    appendObject(sb, depth + 1, value);
                }
            } else {
                appendScalar(sb, depth, label, value);
            }
        }
    }

    private void appendArray(StringBuilder sb, int depth, String label, JsonNode array) {
        int shown = Math.min(array.size(), MAX_LIST_ITEMS);

        // list of plain values (e.g. account numbers) -> single line
        if (allScalars(array)) {
            StringBuilder values = new StringBuilder();
            for (int i = 0; i < shown; i++) {
                if (i > 0) values.append(", ");
                values.append(format(array.get(i)));
            }
            line(sb, depth).append(label != null ? label + ": " : "").append(values);
            appendRemaining(sb, array.size() - shown);
            sb.append('\n');
            return;
        }

        int itemDepth = depth;
        if (label != null) {
            line(sb, depth).append(label).append(" (").append(array.size()).append("):\n");
            itemDepth = depth + 1;
        }
        for (int i = 0; i < shown; i++) {
            JsonNode item = array.get(i);
            if (isFlat(item)) {
                line(sb, itemDepth).append("- ").append(inline(item)).append('\n');
            } else {
                line(sb, itemDepth).append("-\n");
                appendObject(sb, itemDepth + 1, item);
            }
        }
        if (array.size() > shown) {
            line(sb, itemDepth).append("... oraz ").append(array.size() - shown).append(" kolejnych pozycji\n");
        }
    }

    private void appendScalar(StringBuilder sb, int depth, String label, JsonNode value) {
        if (isEmptyValue(value)) {
            return;
        }
        line(sb, depth).append(label).append(": ").append(format(value)).append('\n');
    }

    private void appendRemaining(StringBuilder sb, int remaining) {
        if (remaining > 0) {
            sb.append(" ... oraz ").append(remaining).append(" kolejnych");
        }
    }

    // "Label: value; Label: value" for an object without nested containers
    private String inline(JsonNode object) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, JsonNode> field : object.properties()) {
            if (SKIPPED_FIELDS.contains(field.getKey()) || isEmptyValue(field.getValue())) {
                continue;
            }
            if (!sb.isEmpty()) sb.append("; ");
            sb.append(label(field.getKey())).append(": ").append(format(field.getValue()));
        }
        return sb.toString();
    }

    private String format(JsonNode value) {
        if (value.isBoolean()) {
            return value.asBoolean() ? "tak" : "nie";
        }
        if (value.isNumber()) {
            return value.decimalValue().stripTrailingZeros().toPlainString(); // 5000.00 -> 5000
        }
        String text = value.asString().trim();
        if (DATE_TIME.matcher(text).matches()) {
            return text.substring(0, 10);          // 2026-11-17T18:00:00Z -> 2026-11-17
        }
        if (ENUM_CONSTANT.matcher(text).matches()) {
            return text.replace('_', ' ').toLowerCase(); // BRAK_INFORMACJI -> brak informacji
        }
        return text;
    }

    // camelCase -> "Camel case", unless an explicit label exists
    private String label(String key) {
        String label = LABELS.get(key);
        if (label != null) {
            return label;
        }
        String words = CAMEL_CASE_BOUNDARY.matcher(key).replaceAll(" ").toLowerCase();
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }

    private boolean isEmptyValue(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return true;
        }
        if (node.isContainer()) {
            return node.isEmpty();
        }
        return node.isString() && node.asString().isBlank();
    }

    private boolean isFlat(JsonNode node) {
        if (!node.isObject()) {
            return true;
        }
        for (JsonNode child : node) {
            if (child.isContainer() && !child.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean allScalars(JsonNode array) {
        for (JsonNode item : array) {
            if (item.isContainer()) {
                return false;
            }
        }
        return true;
    }

    private StringBuilder line(StringBuilder sb, int depth) {
        return sb.append(INDENT.repeat(depth));
    }
}
