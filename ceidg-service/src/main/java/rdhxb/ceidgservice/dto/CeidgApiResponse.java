package rdhxb.ceidgservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CeidgApiResponse(
        List<Firma> firma
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Firma(
            String id,
            String nazwa,
            Adres adresDzialalnosci,
            Adres adresKorespondencyjny,
            Wlasciciel wlasciciel,
            List<Obywatelstwo> obywatelstwa,
            String rokPkd,
            List<Pkd> pkd,
            Pkd pkdGlowny,
            LocalDate dataRozpoczecia,
            LocalDate dataZawieszenia,
            LocalDate dataWznowienia,
            LocalDate dataZakonczenia,
            LocalDate dataWykreslenia,
            String status,
            Integer numerStatusu,
            String wspolnoscMajatkowa,
            String telefon,
            String email,
            String www,
            String link
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Adres(
            String ulica,
            String budynek,
            String lokal,
            String miasto,
            String wojewodztwo,
            String powiat,
            String gmina,
            String kraj,
            String kod,
            String adresat,
            String terc,
            String simc,
            String ulic
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wlasciciel(
            String imie,
            String nazwisko,
            String nip,
            String regon
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Obywatelstwo(
            String symbol,
            String kraj
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pkd(
            String kod,
            String nazwa
    ) {}
}
