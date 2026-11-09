package rdhxb.sudopservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SudopApiResponse(
        @JsonProperty("liczba-wynikow") Integer liczbaWynikow,
        @JsonProperty("wyniki") List<Wynik> wyniki
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wynik(
            @JsonProperty("dzien-udzielenia-pomocy") LocalDate dzienUdzieleniaPomocy,
            @JsonProperty("nip-udzielajacego-pomocy") String nipUdzielajacegoPomocy,
            @JsonProperty("nazwa-udzielajacego-pomocy") String nazwaUdzielajacegoPomocy,
            @JsonProperty("srodek-pomocowy-nazwa") String srodekPomocowyNazwa,
            @JsonProperty("przeznaczenie-pomocy-kod") String przeznaczeniePomocyKod,
            @JsonProperty("przeznaczenie-pomocy-nazwa") String przeznaczeniePomocyNazwa,
            @JsonProperty("forma-pomocy-kod") String formaPomocyKod,
            @JsonProperty("forma-pomocy-nazwa") String formaPomocyNazwa,
            @JsonProperty("wartosc-nominalna-pln") BigDecimal wartoscNominalnaPln,
            @JsonProperty("wartosc-brutto-pln") BigDecimal wartoscBruttoPln,
            @JsonProperty("wartosc-brutto-eur") BigDecimal wartoscBruttoEur
    ) {}
}
