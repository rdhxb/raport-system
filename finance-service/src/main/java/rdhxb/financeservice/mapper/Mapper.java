package rdhxb.financeservice.mapper;

import org.springframework.stereotype.Component;
import rdhxb.common.finance.FinanceData;
import rdhxb.common.finance.Pozycja;
import rdhxb.common.finance.Sekcja;
import rdhxb.financeservice.dto.SprawozdanieDto.Bilans;
import rdhxb.financeservice.dto.SprawozdanieDto.Kwota;
import rdhxb.financeservice.dto.SprawozdanieDto.Metadane;
import rdhxb.financeservice.dto.SprawozdanieDto.RachunekZyskowIStrat;
import rdhxb.financeservice.dto.SprawozdanieDto.Sprawozdanie;
import rdhxb.financeservice.entity.SprawozdanieModel;

import java.util.ArrayList;
import java.util.List;

@Component
public class Mapper {

    public FinanceData sprawozdanieModelToData(SprawozdanieModel sprawozdanieModel) {

        Sprawozdanie sprawozdanie = sprawozdanieModel.getSprawozdanie();
        Metadane metadane = sprawozdanie.metadane();

        return new FinanceData(
                sprawozdanieModel.getOkresOd(),
                sprawozdanieModel.getOkresDo(),
                metadane.dataSporzadzenia(),
                metadane.nazwaFirmy(),
                metadane.krs(),
                metadane.nip(),
                metadane.kontynuacjaDzialalnosci(),
                toPozycje(sprawozdanie.bilans(), sprawozdanie.rachunekZyskowIStrat())
        );
    }

    private List<Pozycja> toPozycje(Bilans b, RachunekZyskowIStrat r) {
        List<Pozycja> lista = new ArrayList<>();

        if (b != null) {
            add(lista, Sekcja.AKTYWA, "A",           "Aktywa trwałe",                               b.aktywaTrwale());
            add(lista, Sekcja.AKTYWA, "B",           "Aktywa obrotowe",                             b.aktywaObrotowe());
            add(lista, Sekcja.AKTYWA, "B.I",         "Zapasy",                                      b.zapasy());
            add(lista, Sekcja.AKTYWA, "B.II",        "Należności krótkoterminowe",                  b.naleznosciKrotkoterminowe());
            add(lista, Sekcja.AKTYWA, "B.III.1.c.1", "Środki pieniężne w kasie i na rachunkach",    b.srodkiPieniezne());
            add(lista, Sekcja.AKTYWA, "B.IV",        "Krótkoterminowe rozliczenia międzyokresowe",  b.rozliczeniaMiedzyokresowe());
            add(lista, Sekcja.AKTYWA, "",            "Aktywa razem",                                b.sumaBilansowa());

            add(lista, Sekcja.PASYWA, "A",           "Kapitał (fundusz) własny",                    b.kapitalWlasny());
            add(lista, Sekcja.PASYWA, "A.I",         "Kapitał (fundusz) podstawowy",                b.kapitalZakladowy());
            add(lista, Sekcja.PASYWA, "A.II",        "Kapitał (fundusz) zapasowy",                  b.kapitalZapasowy());
            add(lista, Sekcja.PASYWA, "A.V",         "Zysk (strata) z lat ubiegłych",               b.wynikZLatUbieglych());
            add(lista, Sekcja.PASYWA, "A.VI",        "Zysk (strata) netto",                         b.wynikNetto());
            add(lista, Sekcja.PASYWA, "B",           "Zobowiązania i rezerwy na zobowiązania",      b.zobowiazaniaIRezerwy());
            add(lista, Sekcja.PASYWA, "B.I",         "Rezerwy na zobowiązania",                     b.rezerwy());
            add(lista, Sekcja.PASYWA, "B.II",        "Zobowiązania długoterminowe",                 b.zobowiazaniaDlugoterminowe());
            add(lista, Sekcja.PASYWA, "B.II.3.a",    "Kredyty i pożyczki",                          b.kredytyIPozyczkiDlugoterminowe());
            add(lista, Sekcja.PASYWA, "B.III",       "Zobowiązania krótkoterminowe",                b.zobowiazaniaKrotkoterminowe());
            add(lista, Sekcja.PASYWA, "B.III.3.d",   "Z tytułu dostaw i usług",                     b.zobowiazaniaWobecDostawcow());
            add(lista, Sekcja.PASYWA, "B.III.3.g",   "Z tytułu podatków, ceł, ubezpieczeń",         b.zobowiazaniaPublicznoprawne());
            add(lista, Sekcja.PASYWA, "B.III.3.h",   "Z tytułu wynagrodzeń",                        b.zobowiazaniaZTytuluWynagrodzen());
            add(lista, Sekcja.PASYWA, "",            "Pasywa razem",                                b.sumaBilansowa());
        }

        // null for the RZiSKalk variant (not supported yet)
        if (r != null) {
            add(lista, Sekcja.RZIS, "A",   "Przychody netto ze sprzedaży",              r.przychodyNetto());
            add(lista, Sekcja.RZIS, "B",   "Koszty działalności operacyjnej",           r.kosztyDzialalnosciOperacyjnej());
            add(lista, Sekcja.RZIS, "B.I", "Amortyzacja",                               r.amortyzacja());
            add(lista, Sekcja.RZIS, "B.V", "Wynagrodzenia",                             r.wynagrodzenia());
            add(lista, Sekcja.RZIS, "C",   "Zysk (strata) ze sprzedaży",                r.zyskZeSprzedazy());
            add(lista, Sekcja.RZIS, "D",   "Pozostałe przychody operacyjne",            r.pozostalePrzychodyOperacyjne());
            add(lista, Sekcja.RZIS, "E",   "Pozostałe koszty operacyjne",               r.pozostaleKosztyOperacyjne());
            add(lista, Sekcja.RZIS, "F",   "Zysk (strata) z działalności operacyjnej",  r.zyskOperacyjny());
            add(lista, Sekcja.RZIS, "G",   "Przychody finansowe",                       r.przychodyFinansowe());
            add(lista, Sekcja.RZIS, "H",   "Koszty finansowe",                          r.kosztyFinansowe());
            add(lista, Sekcja.RZIS, "H.I", "Odsetki",                                   r.odsetki());
            add(lista, Sekcja.RZIS, "I",   "Zysk (strata) brutto",                      r.zyskBrutto());
            add(lista, Sekcja.RZIS, "J",   "Podatek dochodowy",                         r.podatekDochodowy());
            add(lista, Sekcja.RZIS, "L",   "Zysk (strata) netto",                       r.zyskNetto());
        }

        return lista;
    }

    private void add(List<Pozycja> lista, Sekcja sekcja, String symbol, String nazwa, Kwota kwota) {
        if (kwota == null) {
            return;
        }
        lista.add(new Pozycja(nazwa, kwota.biezacy(), kwota.poprzedni(), symbol, sekcja));
    }


}
