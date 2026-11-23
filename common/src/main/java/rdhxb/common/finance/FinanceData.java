package rdhxb.common.finance;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FinanceData(

        LocalDate okresOd,
        LocalDate okresDo,
        LocalDate dataSporzadzenia,
        String nazwaFirmy,
        String krs,
        String nip,
        boolean kontynuacjaDzialalnosci,
        List<Pozycja> pozycje
) {

    // KwotaA (current year) and KwotaB (previous year)
    public record Kwota(
            BigDecimal biezacy,
            BigDecimal poprzedni
    ) {}


}
