package rdhxb.common.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record KrsData(
//        --- naglowek ---
        String krsNumber,
        String rejestr,
        LocalDate stanZDnia,
        LocalDate dataRejestracji,
        Integer numerOstatniegoWpisu,
        LocalDate dataOstatniegoWpisu,

//        --- dane podmiotu ---
        String nazwa,
        String formaPrawna,
        String nip,
        String regon,
        Boolean statusOpp,

//        --- adres ---
        String ulica,
        String nrDomu,
        String nrLokalu,
        String miejscowosc,
        String kodPocztowy,
        String poczta,
        String kraj,

//        --- siedziba ---
        String krajSiedziby,
        String wojewodztwo,
        String powiat,
        String gmina,
        String miejscowoscSiedziby,

        String email,
        String www,

//        --- kapital ---
        BigDecimal kapitalZakladowy,
        String kapitalWaluta,

//        --- reprezentacja ---
        String organReprezentacji,
        String sposobReprezentacji
) {}
