package rdhxb.common.finance;

import java.math.BigDecimal;

public record Pozycja(

        String nazwa,
        BigDecimal biezacy,
        BigDecimal poprzedni,
        String symbol,
        Sekcja sekcja
){}
