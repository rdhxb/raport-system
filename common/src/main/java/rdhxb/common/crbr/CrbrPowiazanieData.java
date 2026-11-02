package rdhxb.common.crbr;

import java.math.BigDecimal;

public record CrbrPowiazanieData(

        String pesel,
        String imie,
        String nazwisko,

        String krs,
        String nazwa,
        String nip,

        String rodzajUprawnien,
        BigDecimal ilosc,
        String jednostkaMiary,

        Boolean zrodlowa

) {}
