package rdhxb.summaryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.ceidg.CeidgData;
import rdhxb.common.ceidg.CeidgHistoriaData;
import rdhxb.common.ceidg.CeidgPkdData;
import rdhxb.common.crbr.CrbrData;
import rdhxb.common.crbr.CrbrPowiazanieData;
import rdhxb.common.krs.CzlonekOrganuData;
import rdhxb.common.krs.KrsData;
import rdhxb.common.krs.PkdData;
import rdhxb.common.krs.SprawozdanieFinansoweData;
import rdhxb.common.krs.WspolnikData;
import rdhxb.common.nip.WlData;
import rdhxb.common.sudop.SudopData;
import rdhxb.common.summary.SummaryData;
import rdhxb.summaryservice.client.Client;
import rdhxb.summaryservice.config.RabbitMqConfig;
import rdhxb.summaryservice.prompt.SystemPrompt;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SummaryService {

    // Long lists (PKD, reports) are cut to keep the prompt short for a small model
    private static final int MAX_LIST_ITEMS = 10;
    private static final String NO_DATA = "Brak danych z tego źródła.\n";

    private final RabbitTemplate rabbitTemplate;
    private final Client client;


    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @SendTo(RabbitMqConfig.REPLIES)
    public RepliesModel getRaportFromRaportService(SummaryData data) throws IOException, InterruptedException {

        RepliesModel replies = new RepliesModel();
        replies.setSource(Source.AI);
        replies.setRequestId(data.getRequestId());

        String dataForModel = buildDescription(data);

        var res = client.sendData(SystemPrompt.SUMMARY_FREE, dataForModel);

        if (res != null){
            replies.setPayload(res);
            replies.setStatus(Status.SUCCESS);
            return replies ;
        }

        replies.setStatus(Status.FAILED);
        replies.setErrorMessage("Fail to get response from Jetson !");

        return replies;
    }

    // Builds plain "Label: value" text, one section per source, skipping null / blank values
    private String buildDescription(SummaryData data) {
        KrsData krs = data.getKrs();
        WlData wl = data.getWl();
        CeidgData ceidg = data.getCeidg();
        CrbrData crbr = data.getCrbr();
        SudopData sudop = data.getSudop();

        StringBuilder sb = new StringBuilder();

        sb.append("=== DANE PODSTAWOWE ===\n");
        line(sb, "Nazwa", firstNonBlank(
                krs != null ? krs.nazwa() : null,
                wl != null ? wl.name() : null,
                ceidg != null ? ceidg.nazwa() : null));
        line(sb, "NIP", firstNonBlank(
                krs != null ? krs.nip() : null,
                wl != null ? wl.nip() : null,
                ceidg != null ? ceidg.nip() : null));
        line(sb, "Numer KRS", firstNonBlank(
                krs != null ? krs.krsNumber() : null,
                wl != null ? wl.krs() : null));

        appendKrs(sb, krs);
        appendWl(sb, wl);
        appendCrbr(sb, crbr);
        appendCeidg(sb, ceidg);
        appendSudop(sb, sudop);

        return sb.toString().trim();
    }

    private void appendKrs(StringBuilder sb, KrsData krs) {
        header(sb, "KRS - Krajowy Rejestr Sądowy");
        if (krs == null) {
            sb.append(NO_DATA);
            return;
        }
        line(sb, "Nazwa", krs.nazwa());
        line(sb, "Forma prawna", krs.formaPrawna());
        line(sb, "Rejestr", krs.rejestr());
        line(sb, "Data rejestracji", krs.dataRejestracji());
        line(sb, "Stan z dnia", krs.stanZDnia());
        line(sb, "Data ostatniego wpisu", krs.dataOstatniegoWpisu());
        line(sb, "REGON", krs.regon());
        line(sb, "Status OPP", krs.statusOpp());
        line(sb, "Adres", address(krs.ulica(), krs.nrDomu(), krs.nrLokalu(), krs.kodPocztowy(), krs.miejscowosc()));
        line(sb, "Siedziba", join(", ", krs.miejscowoscSiedziby(), krs.gmina(), krs.powiat(), krs.wojewodztwo()));
        line(sb, "Email", krs.email());
        line(sb, "WWW", krs.www());
        if (krs.kapitalZakladowy() != null) {
            line(sb, "Kapitał zakładowy", join(" ", plain(krs.kapitalZakladowy()), krs.kapitalWaluta()));
        }

        line(sb, "Organ reprezentacji", krs.organReprezentacji());
        line(sb, "Sposób reprezentacji", krs.sposobReprezentacji());
        if (notEmpty(krs.sklad())) {
            sb.append("Skład organu reprezentacji:\n");
            for (CzlonekOrganuData c : krs.sklad()) {
                sb.append("- ").append(join(" ", c.imie(), c.nazwisko()));
                if (!isBlank(c.funkcja())) sb.append(" - ").append(c.funkcja());
                if (Boolean.TRUE.equals(c.zawieszony())) sb.append(" (ZAWIESZONY)");
                sb.append('\n');
            }
        }

        if (notEmpty(krs.wspolnicy())) {
            sb.append("Wspólnicy:\n");
            for (WspolnikData w : krs.wspolnicy()) {
                sb.append("- ").append(w.nazwa());
                if (!isBlank(w.krs())) sb.append("; KRS: ").append(w.krs());
                if (!isBlank(w.udzialy())) sb.append("; udziały: ").append(w.udzialy());
                if (Boolean.TRUE.equals(w.caloscUdzialow())) sb.append("; posiada całość udziałów");
                sb.append('\n');
            }
        }

        pkdList(sb, "Przeważająca działalność (PKD)", krs.przedmiotPrzewazajacejDzialalnosci());
        pkdList(sb, "Pozostała działalność (PKD)", krs.przedmiotPozostalejDzialalnosci());

        List<SprawozdanieFinansoweData> reports = krs.wzmiankaOZlozeniuRocznegoSprawozdaniaFinansowego();
        if (notEmpty(reports)) {
            sb.append("Złożone sprawozdania finansowe (").append(reports.size()).append("):\n");
            // newest reports are the most relevant - take them from the end of the list
            List<SprawozdanieFinansoweData> shown = reports.subList(Math.max(0, reports.size() - MAX_LIST_ITEMS), reports.size());
            for (SprawozdanieFinansoweData s : shown) {
                sb.append("- za okres ").append(s.zaOkresOdDo()).append(", złożone ").append(s.dataZlozenia()).append('\n');
            }
        } else {
            sb.append("Złożone sprawozdania finansowe: brak\n");
        }
    }

    private void appendWl(StringBuilder sb, WlData wl) {
        header(sb, "Biała lista podatników VAT");
        if (wl == null) {
            sb.append(NO_DATA);
            return;
        }
        line(sb, "Nazwa", wl.name());
        line(sb, "Status VAT", wl.statusVat());
        line(sb, "Adres siedziby", wl.residenceAddress());
        line(sb, "Adres prowadzenia działalności", wl.workingAddress());
        line(sb, "Data rejestracji jako podatnik VAT", wl.registrationLegalDate());
        line(sb, "Data odmowy rejestracji VAT", wl.registrationDenialDate());
        line(sb, "Podstawa odmowy rejestracji VAT", wl.registrationDenialBasis());
        line(sb, "Data przywrócenia do rejestru VAT", wl.restorationDate());
        line(sb, "Podstawa przywrócenia do rejestru VAT", wl.restorationBasis());
        line(sb, "Data wykreślenia z rejestru VAT", wl.removalDate());
        line(sb, "Podstawa wykreślenia z rejestru VAT", wl.removalBasis());
        line(sb, "Data zwolnienia SME", wl.exemptionSmeDate());
        // only the count - the prompt forbids quoting account numbers anyway
        int accounts = wl.accountNumbers() != null ? wl.accountNumbers().size() : 0;
        line(sb, "Liczba rachunków bankowych na białej liście", accounts);
        line(sb, "Posiada rachunki wirtualne", wl.hasVirtualAccounts());
    }

    private void appendCrbr(StringBuilder sb, CrbrData crbr) {
        header(sb, "CRBR - Centralny Rejestr Beneficjentów Rzeczywistych");
        if (crbr == null) {
            sb.append(NO_DATA);
            return;
        }
        if (crbr.status() != null) {
            line(sb, "Status", switch (crbr.status()) {
                case ISTNIEJA_INFORMACJE -> "istnieją informacje";
                case BRAK_INFORMACJI -> "brak informacji";
                case BLAD_FORMALNY -> "blad formalny";
            });
        }
        if (notEmpty(crbr.powiazania())) {
            sb.append("Beneficjenci rzeczywiści:\n");
            for (CrbrPowiazanieData p : crbr.powiazania()) {
                // person (imie + nazwisko) or company (nazwa) - PESEL is skipped on purpose
                String who = firstNonBlank(join(" ", p.imie(), p.nazwisko()), p.nazwa());
                sb.append("- ").append(who);
                if (!isBlank(p.rodzajUprawnien())) sb.append("; uprawnienia: ").append(p.rodzajUprawnien());
                if (p.ilosc() != null) sb.append("; ").append(join(" ", plain(p.ilosc()), p.jednostkaMiary()));
                sb.append('\n');
            }
        }
    }

    private void appendCeidg(StringBuilder sb, CeidgData ceidg) {
        header(sb, "CEIDG - Centralna Ewidencja Działalności Gospodarczej");
        if (ceidg == null) {
            sb.append(NO_DATA);
            return;
        }
        line(sb, "Nazwa", ceidg.nazwa());
        line(sb, "Status", ceidg.status());
        line(sb, "Właściciel", join(" ", ceidg.imie(), ceidg.nazwisko()));
        line(sb, "REGON", ceidg.regon());
        line(sb, "Data rozpoczęcia", ceidg.dataRozpoczecia());
        line(sb, "Data zawieszenia", ceidg.dataZawieszenia());
        line(sb, "Data wznowienia", ceidg.dataWznowienia());
        line(sb, "Data zakończenia", ceidg.dataZakonczenia());
        line(sb, "Data wykreślenia", ceidg.dataWykreslenia());
        line(sb, "Adres", address(ceidg.ulica(), ceidg.budynek(), ceidg.lokal(), ceidg.kodPocztowy(), ceidg.miasto()));
        line(sb, "Województwo", ceidg.wojewodztwo());
        line(sb, "WWW", ceidg.www());
        line(sb, "Wspólność majątkowa", ceidg.wspolnoscMajatkowa());
        if (ceidg.pkdGlowny() != null) {
            line(sb, "Główne PKD", join(" - ", ceidg.pkdGlowny().kod(), ceidg.pkdGlowny().nazwa()));
        }
        if (notEmpty(ceidg.pkd())) {
            sb.append("Pozostałe PKD (").append(ceidg.pkd().size()).append("):\n");
            for (CeidgPkdData p : ceidg.pkd().subList(0, Math.min(ceidg.pkd().size(), MAX_LIST_ITEMS))) {
                sb.append("- ").append(join(" - ", p.kod(), p.nazwa())).append('\n');
            }
        }
        if (notEmpty(ceidg.historia())) {
            sb.append("Wcześniejsze działalności tego NIP:\n");
            for (CeidgHistoriaData h : ceidg.historia()) {
                sb.append("- ").append(h.nazwa());
                if (!isBlank(h.status())) sb.append("; status: ").append(h.status());
                if (h.dataRozpoczecia() != null) sb.append("; od ").append(h.dataRozpoczecia());
                if (h.dataZakonczenia() != null) sb.append("; do ").append(h.dataZakonczenia());
                sb.append('\n');
            }
        }
    }

    private void appendSudop(StringBuilder sb, SudopData sudop) {
        header(sb, "SUDOP - otrzymana pomoc publiczna");
        if (sudop == null) {
            sb.append("Brak wpisów.\n");
            return;
        }
        sb.append("- ").append(sudop.dzienUdzieleniaPomocy());
        if (!isBlank(sudop.nazwaUdzielajacegoPomocy())) sb.append("; udzielający: ").append(sudop.nazwaUdzielajacegoPomocy());
        if (!isBlank(sudop.przeznaczeniePomocyNazwa())) sb.append("; przeznaczenie: ").append(sudop.przeznaczeniePomocyNazwa());
        if (!isBlank(sudop.formaPomocyNazwa())) sb.append("; forma: ").append(sudop.formaPomocyNazwa());
        if (sudop.wartoscBruttoPln() != null) sb.append("; wartość brutto: ").append(plain(sudop.wartoscBruttoPln())).append(" PLN");
        sb.append('\n');
    }

    // --- helpers ---

    private void header(StringBuilder sb, String title) {
        sb.append("\n=== ").append(title).append(" ===\n");
    }

    private void line(StringBuilder sb, String label, Object value) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            return;
        }
        String text = switch (value) {
            case Boolean b -> b ? "tak" : "nie";
            case BigDecimal d -> plain(d);
            default -> value.toString();
        };
        sb.append(label).append(": ").append(text).append('\n');
    }

    private void pkdList(StringBuilder sb, String label, List<PkdData> pkd) {
        if (!notEmpty(pkd)) {
            return;
        }
        sb.append(label).append(":\n");
        for (PkdData p : pkd.subList(0, Math.min(pkd.size(), MAX_LIST_ITEMS))) {
            String code = join(".", p.kodDzial(), p.kodKlasa()) + (isBlank(p.kodPodklasa()) ? "" : "." + p.kodPodklasa());
            sb.append("- ").append(join(" - ", code, p.opis())).append('\n');
        }
        if (pkd.size() > MAX_LIST_ITEMS) {
            sb.append("... oraz ").append(pkd.size() - MAX_LIST_ITEMS).append(" kolejnych\n");
        }
    }

    // "ul. X 1/2, 00-000 Miasto"
    private String address(String street, String house, String flat, String postCode, String city) {
        String building = isBlank(flat) ? house : join("/", house, flat);
        return join(", ", join(" ", street, building), join(" ", postCode, city));
    }

    private String join(String separator, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (isBlank(part)) continue;
            if (!sb.isEmpty()) sb.append(separator);
            sb.append(part.trim());
        }
        return sb.toString();
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (!isBlank(v)) return v;
        }
        return null;
    }

    private String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private boolean notEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
}
