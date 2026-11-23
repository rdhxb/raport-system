package rdhxb.financeservice.dto;



import java.math.BigDecimal;
import java.time.LocalDate;


// Structure mirrors the XML (appendix 1 to the Accounting Act); every group has a "razem" total
public class SprawozdanieDto {

    public record Sprawozdanie(
            Metadane metadane,
            Bilans bilans,
            RachunekZyskowIStrat rachunekZyskowIStrat   // null for RZiSKalk (not supported yet)
    ){}

    public record Metadane(
            String typSprawozdania,
            String wersjaSchemy,
            String wariantRzis,
            LocalDate okresOd,
            LocalDate okresDo,
            LocalDate dataSporzadzenia,
            String nazwaFirmy,
            String nip,
            String krs,
            String formaPrawna,
            boolean kontynuacjaDzialalnosci
    ) {}

    // One position of the statement: KwotaA (current year) and KwotaB (previous year)
    public record Kwota(
            BigDecimal biezacy,
            BigDecimal poprzedni
    ){}

    // ===================== Shared shapes =====================

    // Position with a single "w tym" (of which) sub-position
    public record KwotaWTym(
            Kwota razem,
            Kwota wTym
    ){}

    // Split by maturity: up to 12 months / over 12 months
    public record WgTerminu(
            Kwota razem,
            Kwota doRoku,                            // _1
            Kwota powyzejRoku                        // _2
    ){}

    // Split into long-term / short-term part
    public record DlugoIKrotkoterminowe(
            Kwota razem,
            Kwota dlugoterminowe,                    // _1
            Kwota krotkoterminowe                    // _2
    ){}

    // Receivables / liabilities towards related entities or entities with capital involvement
    public record RozrachunkiZJednostka(
            Kwota razem,
            WgTerminu zTytuluDostawIUslug,           // _A
            Kwota inne                               // _B
    ){}

    // Financial assets held in one kind of entity
    public record AktywaFinansowe(
            Kwota razem,
            Kwota udzialyLubAkcje,                   // _1
            Kwota innePapieryWartosciowe,            // _2
            Kwota udzielonePozyczki,                 // _3
            Kwota inneAktywaFinansowe                // _4
    ){}

    // ===================== Bilans =====================

    public record Bilans(
            Aktywa aktywa,                           // Aktywa
            Pasywa pasywa                            // Pasywa
    ){}

    public record Aktywa(
            Kwota razem,                             // Aktywa
            AktywaTrwale aktywaTrwale,               // Aktywa_A
            AktywaObrotowe aktywaObrotowe,           // Aktywa_B
            Kwota naleznaWplatyNaKapitalPodstawowy,  // Aktywa_C
            Kwota udzialyWlasne                      // Aktywa_D
    ){}

    public record AktywaTrwale(
            Kwota razem,                                                     // Aktywa_A
            WartosciNiematerialneIPrawne wartosciNiematerialneIPrawne,       // Aktywa_A_I
            RzeczoweAktywaTrwale rzeczoweAktywaTrwale,                       // Aktywa_A_II
            NaleznosciDlugoterminowe naleznosciDlugoterminowe,               // Aktywa_A_III
            InwestycjeDlugoterminowe inwestycjeDlugoterminowe,               // Aktywa_A_IV
            DlugoterminoweRozliczeniaMiedzyokresowe rozliczeniaMiedzyokresowe // Aktywa_A_V
    ){}

    public record WartosciNiematerialneIPrawne(
            Kwota razem,                             // Aktywa_A_I
            Kwota kosztyPracRozwojowych,             // Aktywa_A_I_1
            Kwota wartoscFirmy,                      // Aktywa_A_I_2
            Kwota inne,                              // Aktywa_A_I_3
            Kwota zaliczki                           // Aktywa_A_I_4
    ){}

    public record RzeczoweAktywaTrwale(
            Kwota razem,                             // Aktywa_A_II
            SrodkiTrwale srodkiTrwale,               // Aktywa_A_II_1
            Kwota srodkiTrwaleWBudowie,              // Aktywa_A_II_2
            Kwota zaliczkiNaSrodkiTrwaleWBudowie     // Aktywa_A_II_3
    ){}

    public record SrodkiTrwale(
            Kwota razem,                             // Aktywa_A_II_1
            Kwota grunty,                            // Aktywa_A_II_1_A
            Kwota budynkiILokale,                    // Aktywa_A_II_1_B
            Kwota urzadzeniaTechniczneIMaszyny,      // Aktywa_A_II_1_C
            Kwota srodkiTransportu,                  // Aktywa_A_II_1_D
            Kwota inne                               // Aktywa_A_II_1_E
    ){}

    public record NaleznosciDlugoterminowe(
            Kwota razem,                             // Aktywa_A_III
            Kwota odJednostekPowiazanych,            // Aktywa_A_III_1
            Kwota odJednostekZZaangazowaniem,        // Aktywa_A_III_2
            Kwota odPozostalychJednostek             // Aktywa_A_III_3
    ){}

    public record InwestycjeDlugoterminowe(
            Kwota razem,                                             // Aktywa_A_IV
            Kwota nieruchomosci,                                     // Aktywa_A_IV_1
            Kwota wartosciNiematerialneIPrawne,                      // Aktywa_A_IV_2
            DlugoterminoweAktywaFinansowe aktywaFinansowe,           // Aktywa_A_IV_3
            Kwota inne                                               // Aktywa_A_IV_4
    ){}

    public record DlugoterminoweAktywaFinansowe(
            Kwota razem,                                     // Aktywa_A_IV_3
            AktywaFinansowe wJednostkachPowiazanych,         // Aktywa_A_IV_3_A
            AktywaFinansowe wJednostkachZZaangazowaniem,     // Aktywa_A_IV_3_B
            AktywaFinansowe wPozostalychJednostkach          // Aktywa_A_IV_3_C
    ){}

    public record DlugoterminoweRozliczeniaMiedzyokresowe(
            Kwota razem,                             // Aktywa_A_V
            Kwota aktywaZTytuluOdroczonegoPodatku,   // Aktywa_A_V_1
            Kwota inne                               // Aktywa_A_V_2
    ){}

    public record AktywaObrotowe(
            Kwota razem,                                             // Aktywa_B
            Zapasy zapasy,                                           // Aktywa_B_I
            NaleznosciKrotkoterminowe naleznosciKrotkoterminowe,     // Aktywa_B_II
            InwestycjeKrotkoterminowe inwestycjeKrotkoterminowe,     // Aktywa_B_III
            Kwota rozliczeniaMiedzyokresowe                          // Aktywa_B_IV
    ){}

    public record Zapasy(
            Kwota razem,                             // Aktywa_B_I
            Kwota materialy,                         // Aktywa_B_I_1
            Kwota polproduktyIProduktyWToku,         // Aktywa_B_I_2
            Kwota produktyGotowe,                    // Aktywa_B_I_3
            Kwota towary,                            // Aktywa_B_I_4
            Kwota zaliczkiNaDostawyIUslugi           // Aktywa_B_I_5
    ){}

    public record NaleznosciKrotkoterminowe(
            Kwota razem,                                             // Aktywa_B_II
            RozrachunkiZJednostka odJednostekPowiazanych,            // Aktywa_B_II_1
            RozrachunkiZJednostka odJednostekZZaangazowaniem,        // Aktywa_B_II_2
            NaleznosciOdPozostalychJednostek odPozostalychJednostek  // Aktywa_B_II_3
    ){}

    public record NaleznosciOdPozostalychJednostek(
            Kwota razem,                             // Aktywa_B_II_3
            WgTerminu zTytuluDostawIUslug,           // Aktywa_B_II_3_A
            Kwota zTytuluPodatkow,                   // Aktywa_B_II_3_B (taxes, subsidies, social security)
            Kwota inne,                              // Aktywa_B_II_3_C
            Kwota dochodzoneNaDrodzeSadowej          // Aktywa_B_II_3_D
    ){}

    public record InwestycjeKrotkoterminowe(
            Kwota razem,                                             // Aktywa_B_III
            KrotkoterminoweAktywaFinansowe aktywaFinansowe,          // Aktywa_B_III_1
            Kwota inne                                               // Aktywa_B_III_2
    ){}

    public record KrotkoterminoweAktywaFinansowe(
            Kwota razem,                                     // Aktywa_B_III_1
            AktywaFinansowe wJednostkachPowiazanych,         // Aktywa_B_III_1_A
            AktywaFinansowe wPozostalychJednostkach,         // Aktywa_B_III_1_B
            SrodkiPieniezne srodkiPieniezne                  // Aktywa_B_III_1_C
    ){}

    public record SrodkiPieniezne(
            Kwota razem,                             // Aktywa_B_III_1_C
            Kwota wKasieINaRachunkach,               // Aktywa_B_III_1_C_1
            Kwota inneSrodkiPieniezne,               // Aktywa_B_III_1_C_2
            Kwota inneAktywaPieniezne                // Aktywa_B_III_1_C_3
    ){}

    public record Pasywa(
            Kwota razem,                                     // Pasywa
            KapitalWlasny kapitalWlasny,                     // Pasywa_A
            ZobowiazaniaIRezerwy zobowiazaniaIRezerwy        // Pasywa_B
    ){}

    public record KapitalWlasny(
            Kwota razem,                                             // Pasywa_A
            Kwota kapitalPodstawowy,                                 // Pasywa_A_I
            KapitalZapasowy kapitalZapasowy,                         // Pasywa_A_II
            KapitalZAktualizacjiWyceny kapitalZAktualizacjiWyceny,   // Pasywa_A_III
            PozostaleKapitalyRezerwowe pozostaleKapitalyRezerwowe,   // Pasywa_A_IV
            Kwota zyskZLatUbieglych,                                 // Pasywa_A_V
            Kwota zyskNetto,                                         // Pasywa_A_VI
            Kwota odpisyZZyskuNetto                                  // Pasywa_A_VII (negative value)
    ){}

    public record KapitalZapasowy(
            Kwota razem,                             // Pasywa_A_II
            Kwota nadwyzkaWartosciEmisyjnej          // Pasywa_A_II_1
    ){}

    public record KapitalZAktualizacjiWyceny(
            Kwota razem,                             // Pasywa_A_III
            Kwota zTytuluWartosciGodziwej            // Pasywa_A_III_1
    ){}

    public record PozostaleKapitalyRezerwowe(
            Kwota razem,                             // Pasywa_A_IV
            Kwota zgodnieZUmowaSpolki,               // Pasywa_A_IV_1
            Kwota naUdzialyWlasne                    // Pasywa_A_IV_2
    ){}

    public record ZobowiazaniaIRezerwy(
            Kwota razem,                                                 // Pasywa_B
            Rezerwy rezerwy,                                             // Pasywa_B_I
            ZobowiazaniaDlugoterminowe zobowiazaniaDlugoterminowe,       // Pasywa_B_II
            ZobowiazaniaKrotkoterminowe zobowiazaniaKrotkoterminowe,     // Pasywa_B_III
            BierneRozliczeniaMiedzyokresowe rozliczeniaMiedzyokresowe    // Pasywa_B_IV
    ){}

    public record Rezerwy(
            Kwota razem,                                     // Pasywa_B_I
            Kwota zTytuluOdroczonegoPodatku,                 // Pasywa_B_I_1
            DlugoIKrotkoterminowe naSwiadczeniaEmerytalne,   // Pasywa_B_I_2
            DlugoIKrotkoterminowe pozostale                  // Pasywa_B_I_3
    ){}

    public record ZobowiazaniaDlugoterminowe(
            Kwota razem,                                                         // Pasywa_B_II
            Kwota wobecJednostekPowiazanych,                                     // Pasywa_B_II_1
            Kwota wobecJednostekZZaangazowaniem,                                 // Pasywa_B_II_2
            ZobowiazaniaDlugoterminoweWobecPozostalych wobecPozostalychJednostek // Pasywa_B_II_3
    ){}

    public record ZobowiazaniaDlugoterminoweWobecPozostalych(
            Kwota razem,                             // Pasywa_B_II_3
            Kwota kredytyIPozyczki,                  // Pasywa_B_II_3_A
            Kwota zTytuluEmisjiPapierowDluznych,     // Pasywa_B_II_3_B
            Kwota inneZobowiazaniaFinansowe,         // Pasywa_B_II_3_C
            Kwota zobowiazaniaWekslowe,              // Pasywa_B_II_3_D
            Kwota inne                               // Pasywa_B_II_3_E
    ){}

    public record ZobowiazaniaKrotkoterminowe(
            Kwota razem,                                                          // Pasywa_B_III
            RozrachunkiZJednostka wobecJednostekPowiazanych,                      // Pasywa_B_III_1
            RozrachunkiZJednostka wobecJednostekZZaangazowaniem,                  // Pasywa_B_III_2
            ZobowiazaniaKrotkoterminoweWobecPozostalych wobecPozostalychJednostek, // Pasywa_B_III_3
            Kwota funduszeSpecjalne                                               // Pasywa_B_III_4
    ){}

    public record ZobowiazaniaKrotkoterminoweWobecPozostalych(
            Kwota razem,                             // Pasywa_B_III_3
            Kwota kredytyIPozyczki,                  // Pasywa_B_III_3_A
            Kwota zTytuluEmisjiPapierowDluznych,     // Pasywa_B_III_3_B
            Kwota inneZobowiazaniaFinansowe,         // Pasywa_B_III_3_C
            WgTerminu zTytuluDostawIUslug,           // Pasywa_B_III_3_D
            Kwota zaliczkiOtrzymane,                 // Pasywa_B_III_3_E
            Kwota zobowiazaniaWekslowe,              // Pasywa_B_III_3_F
            Kwota zTytuluPodatkow,                   // Pasywa_B_III_3_G (taxes, social security)
            Kwota zTytuluWynagrodzen,                // Pasywa_B_III_3_H
            Kwota inne                               // Pasywa_B_III_3_I
    ){}

    public record BierneRozliczeniaMiedzyokresowe(
            Kwota razem,                             // Pasywa_B_IV
            Kwota ujemnaWartoscFirmy,                // Pasywa_B_IV_1
            DlugoIKrotkoterminowe inne               // Pasywa_B_IV_2
    ){}

    // ===================== Rachunek zysków i strat =====================

    // Comparative variant (RZiSPor)
    public record RachunekZyskowIStrat(
            PrzychodyNetto przychodyNetto,                               // A
            KosztyDzialalnosciOperacyjnej kosztyDzialalnosciOperacyjnej, // B
            Kwota zyskZeSprzedazy,                                       // C
            PozostalePrzychodyOperacyjne pozostalePrzychodyOperacyjne,   // D
            PozostaleKosztyOperacyjne pozostaleKosztyOperacyjne,         // E
            Kwota zyskOperacyjny,                                        // F
            PrzychodyFinansowe przychodyFinansowe,                       // G
            KosztyFinansowe kosztyFinansowe,                             // H
            Kwota zyskBrutto,                                            // I
            Kwota podatekDochodowy,                                      // J
            Kwota pozostaleObowiazkoweZmniejszeniaZysku,                 // K
            Kwota zyskNetto                                              // L
    ){}

    public record PrzychodyNetto(
            Kwota razem,                             // A
            Kwota odJednostekPowiazanych,            // A_J ("w tym")
            Kwota zeSprzedazyProduktow,              // A_I
            Kwota zmianaStanuProduktow,              // A_II
            Kwota kosztWytworzeniaNaWlasnePotrzeby,  // A_III
            Kwota zeSprzedazyTowarowIMaterialow      // A_IV
    ){}

    public record KosztyDzialalnosciOperacyjnej(
            Kwota razem,                             // B
            Kwota amortyzacja,                       // B_I
            Kwota zuzycieMaterialowIEnergii,         // B_II
            Kwota uslugiObce,                        // B_III
            KwotaWTym podatkiIOplaty,                // B_IV (w tym: excise tax B_IV_1)
            Kwota wynagrodzenia,                     // B_V
            KwotaWTym ubezpieczeniaSpoleczne,        // B_VI (w tym: pension B_VI_1)
            Kwota pozostaleKosztyRodzajowe,          // B_VII
            Kwota wartoscSprzedanychTowarow          // B_VIII
    ){}

    public record PozostalePrzychodyOperacyjne(
            Kwota razem,                                     // D
            Kwota zyskZRozchoduAktywowNiefinansowych,        // D_I
            Kwota dotacje,                                   // D_II
            Kwota aktualizacjaWartosciAktywowNiefinansowych, // D_III
            Kwota inne                                       // D_IV
    ){}

    public record PozostaleKosztyOperacyjne(
            Kwota razem,                                     // E
            Kwota strataZRozchoduAktywowNiefinansowych,      // E_I
            Kwota aktualizacjaWartosciAktywowNiefinansowych, // E_II
            Kwota inne                                       // E_III
    ){}

    public record PrzychodyFinansowe(
            Kwota razem,                                     // G
            Dywidendy dywidendyIUdzialyWZyskach,             // G_I
            KwotaWTym odsetki,                               // G_II (w tym: from related G_II_J)
            KwotaWTym zyskZRozchoduAktywowFinansowych,       // G_III (w tym: in related G_III_J)
            Kwota aktualizacjaWartosciAktywowFinansowych,    // G_IV
            Kwota inne                                       // G_V
    ){}

    public record Dywidendy(
            Kwota razem,                                     // G_I
            KwotaWTym odJednostekPowiazanych,                // G_I_A (w tym: with capital involvement G_I_A_1)
            KwotaWTym odJednostekPozostalych                 // G_I_B (w tym: with capital involvement G_I_B_1)
    ){}

    public record KosztyFinansowe(
            Kwota razem,                                     // H
            KwotaWTym odsetki,                               // H_I (w tym: for related H_I_J)
            KwotaWTym strataZRozchoduAktywowFinansowych,     // H_II (w tym: in related H_II_J)
            Kwota aktualizacjaWartosciAktywowFinansowych,    // H_III
            Kwota inne                                       // H_IV
    ){}

}
