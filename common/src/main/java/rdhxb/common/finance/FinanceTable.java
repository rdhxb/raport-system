package rdhxb.common.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record FinanceTable(
        String nazwa,
        String krs,
        String nip,
        List<Integer> lata,
        List<FinanceRow> rows,
        List<WgraneSprawozdanie> wgraneSprawozdania
) {

    public record FinanceRow(
            Sekcja sekcja,
            String symbol,
            String nazwa,
            Map<Integer, BigDecimal> wartosci, // rok -> kwota
            Map<Integer, BigDecimal> zmiana, // rok -> zmiana % wzgledem roku poprzedniego
            Map<Integer, Boolean> isPositive //rok -> true if prev < curr
    ) {}

    public record WgraneSprawozdanie(
            int rok,
            LocalDate okresOd,
            LocalDate okresDo,
            LocalDate dataSporzadzenia,
            boolean czyKontynuacja
    ){}
}

