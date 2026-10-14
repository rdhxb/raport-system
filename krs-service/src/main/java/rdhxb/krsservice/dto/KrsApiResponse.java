package rdhxb.krsservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record KrsApiResponse(Odpis odpis) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Odpis(
            String rodzaj,
            NaglowekA naglowekA,
            Dane dane
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NaglowekA(
            String rejestr,
            String numerKRS,
            String stanZDnia,
            String dataRejestracjiWKRS,
            Integer numerOstatniegoWpisu,
            String dataOstatniegoWpisu
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dane(
            Dzial1 dzial1,
            Dzial2 dzial2,
            Dzial3 dzial3
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dzial1(
            DanePodmiotu danePodmiotu,
            SiedzibaIAdres siedzibaIAdres,
            List<Wspolnik> wspolnicySpzoo,
            Kapital kapital
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DanePodmiotu(
            String formaPrawna,
            Identyfikatory identyfikatory,
            String nazwa,
            Boolean czyPosiadaStatusOPP
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Identyfikatory(
            String regon,
            String nip
            ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SiedzibaIAdres(
            Siedziba siedziba,
            Adres adres
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Siedziba(
            String kraj,
            String wojewodztwo,
            String powiat,
            String gmina,
            String miejscowosc
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Adres(
            String ulica,
            String nrDomu,
            String nrLokalu,
            String miejscowosc,
            String kodPocztowy,
            String poczta,
            String kraj
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wspolnik(
            String nazwa,
            Identyfikatory identyfikator,
            KrsRef krs,
            String posiadaneUdzialy,
            Boolean czyPosiadaCaloscUdzialow
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KrsRef(
            String krs
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Kapital(
            Kwota wysokoscKapitaluZakladowego
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Kwota(
            String wartosc,
            String waluta
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dzial2(
            Reprezentacja reprezentacja
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Reprezentacja(
            String nazwaOrganu,
            String sposobReprezentacji,
            List<CzlonekOrganu> sklad
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CzlonekOrganu(
            Nazwisko nazwisko,
            Imiona imiona,
            String funkcjaWOrganie,
            Boolean czyZawieszona
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Nazwisko(
            String nazwiskoICzlon
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Imiona(
            String imie
    ) {}


    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Dzial3(
            PrzedmiotDzialalnosci przedmiotDzialalnosci,
            WzmiankiOZlozonychDokumentach wzmiankiOZlozonychDokumentach
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PrzedmiotDzialalnosci(
            List<Pkd> przedmiotPrzewazajacejDzialalnosci,
            List<Pkd> przedmiotPozostalejDzialalnosci
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pkd(
            String opis,
            String kodDzial,
            String kodKlasa,
            String kodPodklasa
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WzmiankiOZlozonychDokumentach(
            List<SprawozdanieFinansowe> wzmiankaOZlozeniuRocznegoSprawozdaniaFinansowego
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SprawozdanieFinansowe(
            String dataZlozenia,
            String zaOkresOdDo
    ) {}
}
