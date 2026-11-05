package rdhxb.common.krs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        String sposobReprezentacji,
        List<CzlonekOrganuData> sklad,

//        --- przedmiot dzialnosci ---
        List<PkdData> przedmiotPrzewazajacejDzialalnosci,
        List<PkdData> przedmiotPozostalejDzialalnosci,

//        --- dokumenty ---
        List<SprawozdanieFinansoweData> wzmiankaOZlozeniuRocznegoSprawozdaniaFinansowego,

        List<WspolnikData> wspolnicy


        ) {}
