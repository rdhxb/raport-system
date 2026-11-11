package rdhxb.common.sudop;

import java.math.BigDecimal;
import java.time.LocalDate;

// One granted public aid from SUDOP (payload of SUDOP reply is a list of these)
public record SudopData(
        LocalDate dzienUdzieleniaPomocy,
        String nipUdzielajacegoPomocy,
        String nazwaUdzielajacegoPomocy,
        String srodekPomocowyNazwa,
        String przeznaczeniePomocyKod,
        String przeznaczeniePomocyNazwa,
        String formaPomocyKod,
        String formaPomocyNazwa,
        BigDecimal wartoscNominalnaPln,
        BigDecimal wartoscBruttoPln,
        BigDecimal wartoscBruttoEur
) {}
