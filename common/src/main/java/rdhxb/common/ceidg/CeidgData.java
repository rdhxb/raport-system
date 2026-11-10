package rdhxb.common.ceidg;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CeidgData (

        Instant fetchedAt,

        String nip,

        Boolean maAktualnaFirme,

        String ceidgId,
        String nazwa,
        String status,
        Integer numerStatusu,

        String imie,
        String nazwisko,
        String regon,
        List<String> obywatelstwa,

        LocalDate dataRozpoczecia,
        LocalDate dataZawieszenia,
        LocalDate dataWznowienia,
        LocalDate dataZakonczenia,
        LocalDate dataWykreslenia,

        String ulica,
        String budynek,
        String lokal,
        String miasto,
        String kodPocztowy,
        String wojewodztwo,
        String powiat,
        String gmina,
        String kraj,

        String adresKorespondencyjny,

        String telefon,
        String email,
        String www,

        String wspolnoscMajatkowa,

        String rokPkd,
        CeidgPkdData pkdGlowny,
        List<CeidgPkdData> pkd,

        List<CeidgHistoriaData> historia


) {}
