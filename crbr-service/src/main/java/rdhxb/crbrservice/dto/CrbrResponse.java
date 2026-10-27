package rdhxb.crbrservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record CrbrResponse(
        String identyfikatorWniosku,
        LocalDateTime dataZlozeniaWniosku,
        LocalDateTime dataUdostepnieniaWniosku,
        StatusCrbr status,
        List<Spolka> spolki
) {

    public enum StatusCrbr {
        ISTNIEJA_INFORMACJE, BRAK_INFORMACJI, BLAD_FORMALNY
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Spolka(
            String nazwa,
            String nip,
            String krs,
            String formaOrganizacyjna,
            Adres adres,
            String numerReferencyjny,
            LocalDate dataPoczatkuPrezentacji,
            LocalDate dataKoncaPrezentacji,
            String skorygowane,
            String postepowanie,
            List<Beneficjent> beneficjenci,
            List<Zglaszajacy> zglaszajacy,
            List<Rozbieznosc> rozbieznosci
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Adres(
            String ulica,
            String nrDomu,
            String nrLokalu,
            String kodPocztowy,
            String miejscowosc,
            String wojewodztwo,
            String powiat,
            String gmina,
            String kraj
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Beneficjent(
            String imie,
            String kolejneImiona,
            String nazwisko,
            String pesel,
            LocalDate dataUrodzenia,
            List<String> obywatelstwa,
            String krajZamieszkania,
            List<Udzial> udzialy,
            String nazwaBeneficjentaGrupowego,
            String uprawnieniaBeneficjentaGrupowego
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Udzial(
            String rodzajUprawnien,
            Double ilosc,
            String jednostkaMiary,
            List<String> rodzajeUprzywilejowania,
            String opisUprzywilejowania,
            String uprawnieniaPosrednie,
            String rodzajInnychUprawnien,
            String opisInnychUprawnien
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Zglaszajacy(
            String imie,
            String kolejneImiona,
            String nazwisko,
            String pesel,
            LocalDate dataUrodzenia,
            List<String> funkcje,
            String inneInformacje
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Rozbieznosc(
            String identyfikatorUwagi,
            String informacja
    ) {}
}
