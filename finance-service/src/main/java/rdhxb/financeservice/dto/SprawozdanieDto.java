package rdhxb.financeservice.dto;



import java.math.BigDecimal;
import java.time.LocalDate;


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

    public record Bilans(
            Kwota sumaBilansowa,                     // Aktywa
            Kwota aktywaTrwale,                      // Aktywa_A
            Kwota aktywaObrotowe,                    // Aktywa_B
            Kwota zapasy,                            // Aktywa_B_I
            Kwota naleznosciKrotkoterminowe,         // Aktywa_B_II
            Kwota srodkiPieniezne,                   // Aktywa_B_III_1_C_1
            Kwota rozliczeniaMiedzyokresowe,         // Aktywa_B_IV
            Kwota kapitalWlasny,                     // Pasywa_A
            Kwota kapitalZakladowy,                  // Pasywa_A_I
            Kwota kapitalZapasowy,                   // Pasywa_A_II
            Kwota wynikZLatUbieglych,                // Pasywa_A_V
            Kwota wynikNetto,                        // Pasywa_A_VI
            Kwota zobowiazaniaIRezerwy,              // Pasywa_B
            Kwota rezerwy,                           // Pasywa_B_I
            Kwota zobowiazaniaDlugoterminowe,        // Pasywa_B_II
            Kwota kredytyIPozyczkiDlugoterminowe,    // Pasywa_B_II_3_A
            Kwota zobowiazaniaKrotkoterminowe,       // Pasywa_B_III
            Kwota zobowiazaniaWobecDostawcow,        // Pasywa_B_III_3_D
            Kwota zobowiazaniaPublicznoprawne,       // Pasywa_B_III_3_G (taxes, social security)
            Kwota zobowiazaniaZTytuluWynagrodzen     // Pasywa_B_III_3_H
    ){}

    // Comparative variant (RZiSPor)
    public record RachunekZyskowIStrat(
            Kwota przychodyNetto,                    // A
            Kwota kosztyDzialalnosciOperacyjnej,     // B
            Kwota amortyzacja,                       // B_I
            Kwota wynagrodzenia,                     // B_V
            Kwota zyskZeSprzedazy,                   // C
            Kwota pozostalePrzychodyOperacyjne,      // D
            Kwota pozostaleKosztyOperacyjne,         // E
            Kwota zyskOperacyjny,                    // F
            Kwota przychodyFinansowe,                // G
            Kwota kosztyFinansowe,                   // H
            Kwota odsetki,                           // H_I
            Kwota zyskBrutto,                        // I
            Kwota podatekDochodowy,                  // J
            Kwota zyskNetto                          // L
    ){}

}
