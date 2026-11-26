package rdhxb.financeservice.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rdhxb.common.finance.FinanceData;
import rdhxb.common.finance.FinanceTable;
import rdhxb.common.finance.FinanceTable.FinanceRow;
import rdhxb.common.finance.FinanceTable.WgraneSprawozdanie;
import rdhxb.common.finance.Pozycja;
import rdhxb.common.finance.Sekcja;
import rdhxb.financeservice.dto.SprawozdanieDto.*;
import rdhxb.financeservice.entity.SprawozdanieModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
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

        Aktywa aktywa = b == null ? null : b.aktywa();
        if (aktywa != null) {
            AktywaTrwale at = aktywa.aktywaTrwale();
            AktywaObrotowe ao = aktywa.aktywaObrotowe();

            add(lista, Sekcja.AKTYWA, "A",     "Aktywa trwałe",                              razem(at, AktywaTrwale::razem));
            add(lista, Sekcja.AKTYWA, "A.I",   "Wartości niematerialne i prawne",            razem(at, x -> razem(x.wartosciNiematerialneIPrawne(), WartosciNiematerialneIPrawne::razem)));
            add(lista, Sekcja.AKTYWA, "A.II",  "Rzeczowe aktywa trwałe",                     razem(at, x -> razem(x.rzeczoweAktywaTrwale(), RzeczoweAktywaTrwale::razem)));
            add(lista, Sekcja.AKTYWA, "A.III", "Należności długoterminowe",                  razem(at, x -> razem(x.naleznosciDlugoterminowe(), NaleznosciDlugoterminowe::razem)));
            add(lista, Sekcja.AKTYWA, "A.IV",  "Inwestycje długoterminowe",                  razem(at, x -> razem(x.inwestycjeDlugoterminowe(), InwestycjeDlugoterminowe::razem)));
            add(lista, Sekcja.AKTYWA, "A.V",   "Długoterminowe rozliczenia międzyokresowe",  razem(at, x -> razem(x.rozliczeniaMiedzyokresowe(), DlugoterminoweRozliczeniaMiedzyokresowe::razem)));

            add(lista, Sekcja.AKTYWA, "B",     "Aktywa obrotowe",                            razem(ao, AktywaObrotowe::razem));
            add(lista, Sekcja.AKTYWA, "B.I",   "Zapasy",                                     razem(ao, x -> razem(x.zapasy(), Zapasy::razem)));
            add(lista, Sekcja.AKTYWA, "B.II",  "Należności krótkoterminowe",                 razem(ao, x -> razem(x.naleznosciKrotkoterminowe(), NaleznosciKrotkoterminowe::razem)));
            add(lista, Sekcja.AKTYWA, "B.III", "Inwestycje krótkoterminowe",                 razem(ao, x -> razem(x.inwestycjeKrotkoterminowe(), InwestycjeKrotkoterminowe::razem)));
            add(lista, Sekcja.AKTYWA, "B.IV",  "Krótkoterminowe rozliczenia międzyokresowe", razem(ao, AktywaObrotowe::rozliczeniaMiedzyokresowe));

            add(lista, Sekcja.AKTYWA, "C",     "Należne wpłaty na kapitał (fundusz) podstawowy", aktywa.naleznaWplatyNaKapitalPodstawowy());
            add(lista, Sekcja.AKTYWA, "D",     "Udziały (akcje) własne",                     aktywa.udzialyWlasne());
            add(lista, Sekcja.AKTYWA, "",      "Aktywa razem",                               aktywa.razem());
        }

        Pasywa pasywa = b == null ? null : b.pasywa();
        if (pasywa != null) {
            KapitalWlasny kw = pasywa.kapitalWlasny();
            ZobowiazaniaIRezerwy zr = pasywa.zobowiazaniaIRezerwy();

            add(lista, Sekcja.PASYWA, "A",     "Kapitał (fundusz) własny",                   razem(kw, KapitalWlasny::razem));
            add(lista, Sekcja.PASYWA, "A.I",   "Kapitał (fundusz) podstawowy",               razem(kw, KapitalWlasny::kapitalPodstawowy));
            add(lista, Sekcja.PASYWA, "A.II",  "Kapitał (fundusz) zapasowy",                 razem(kw, x -> razem(x.kapitalZapasowy(), KapitalZapasowy::razem)));
            add(lista, Sekcja.PASYWA, "A.III", "Kapitał (fundusz) z aktualizacji wyceny",    razem(kw, x -> razem(x.kapitalZAktualizacjiWyceny(), KapitalZAktualizacjiWyceny::razem)));
            add(lista, Sekcja.PASYWA, "A.IV",  "Pozostałe kapitały (fundusze) rezerwowe",    razem(kw, x -> razem(x.pozostaleKapitalyRezerwowe(), PozostaleKapitalyRezerwowe::razem)));
            add(lista, Sekcja.PASYWA, "A.V",   "Zysk (strata) z lat ubiegłych",              razem(kw, KapitalWlasny::zyskZLatUbieglych));
            add(lista, Sekcja.PASYWA, "A.VI",  "Zysk (strata) netto",                        razem(kw, KapitalWlasny::zyskNetto));
            add(lista, Sekcja.PASYWA, "A.VII", "Odpisy z zysku netto w ciągu roku obrotowego", razem(kw, KapitalWlasny::odpisyZZyskuNetto));

            add(lista, Sekcja.PASYWA, "B",     "Zobowiązania i rezerwy na zobowiązania",     razem(zr, ZobowiazaniaIRezerwy::razem));
            add(lista, Sekcja.PASYWA, "B.I",   "Rezerwy na zobowiązania",                    razem(zr, x -> razem(x.rezerwy(), Rezerwy::razem)));
            add(lista, Sekcja.PASYWA, "B.II",  "Zobowiązania długoterminowe",                razem(zr, x -> razem(x.zobowiazaniaDlugoterminowe(), ZobowiazaniaDlugoterminowe::razem)));
            add(lista, Sekcja.PASYWA, "B.III", "Zobowiązania krótkoterminowe",               razem(zr, x -> razem(x.zobowiazaniaKrotkoterminowe(), ZobowiazaniaKrotkoterminowe::razem)));
            add(lista, Sekcja.PASYWA, "B.IV",  "Rozliczenia międzyokresowe",                 razem(zr, x -> razem(x.rozliczeniaMiedzyokresowe(), BierneRozliczeniaMiedzyokresowe::razem)));

            add(lista, Sekcja.PASYWA, "",      "Pasywa razem",                               pasywa.razem());
        }

        // null for the RZiSKalk variant (not supported yet)
        if (r != null) {
            PrzychodyNetto p = r.przychodyNetto();
            KosztyDzialalnosciOperacyjnej k = r.kosztyDzialalnosciOperacyjnej();

            add(lista, Sekcja.RZIS, "A",      "Przychody netto ze sprzedaży i zrównane z nimi",   razem(p, PrzychodyNetto::razem));
            add(lista, Sekcja.RZIS, "A.I",    "Przychody netto ze sprzedaży produktów",           razem(p, PrzychodyNetto::zeSprzedazyProduktow));
            add(lista, Sekcja.RZIS, "A.II",   "Zmiana stanu produktów",                           razem(p, PrzychodyNetto::zmianaStanuProduktow));
            add(lista, Sekcja.RZIS, "A.III",  "Koszt wytworzenia produktów na własne potrzeby",   razem(p, PrzychodyNetto::kosztWytworzeniaNaWlasnePotrzeby));
            add(lista, Sekcja.RZIS, "A.IV",   "Przychody netto ze sprzedaży towarów i materiałów", razem(p, PrzychodyNetto::zeSprzedazyTowarowIMaterialow));

            add(lista, Sekcja.RZIS, "B",      "Koszty działalności operacyjnej",                  razem(k, KosztyDzialalnosciOperacyjnej::razem));
            add(lista, Sekcja.RZIS, "B.I",    "Amortyzacja",                                      razem(k, KosztyDzialalnosciOperacyjnej::amortyzacja));
            add(lista, Sekcja.RZIS, "B.II",   "Zużycie materiałów i energii",                     razem(k, KosztyDzialalnosciOperacyjnej::zuzycieMaterialowIEnergii));
            add(lista, Sekcja.RZIS, "B.III",  "Usługi obce",                                      razem(k, KosztyDzialalnosciOperacyjnej::uslugiObce));
            add(lista, Sekcja.RZIS, "B.IV",   "Podatki i opłaty",                                 razem(k, x -> razem(x.podatkiIOplaty(), KwotaWTym::razem)));
            add(lista, Sekcja.RZIS, "B.V",    "Wynagrodzenia",                                    razem(k, KosztyDzialalnosciOperacyjnej::wynagrodzenia));
            add(lista, Sekcja.RZIS, "B.VI",   "Ubezpieczenia społeczne i inne świadczenia",       razem(k, x -> razem(x.ubezpieczeniaSpoleczne(), KwotaWTym::razem)));
            add(lista, Sekcja.RZIS, "B.VII",  "Pozostałe koszty rodzajowe",                       razem(k, KosztyDzialalnosciOperacyjnej::pozostaleKosztyRodzajowe));
            add(lista, Sekcja.RZIS, "B.VIII", "Wartość sprzedanych towarów i materiałów",         razem(k, KosztyDzialalnosciOperacyjnej::wartoscSprzedanychTowarow));

            add(lista, Sekcja.RZIS, "C",      "Zysk (strata) ze sprzedaży",                       r.zyskZeSprzedazy());
            add(lista, Sekcja.RZIS, "D",      "Pozostałe przychody operacyjne",                   razem(r.pozostalePrzychodyOperacyjne(), PozostalePrzychodyOperacyjne::razem));
            add(lista, Sekcja.RZIS, "E",      "Pozostałe koszty operacyjne",                      razem(r.pozostaleKosztyOperacyjne(), PozostaleKosztyOperacyjne::razem));
            add(lista, Sekcja.RZIS, "F",      "Zysk (strata) z działalności operacyjnej",         r.zyskOperacyjny());
            add(lista, Sekcja.RZIS, "G",      "Przychody finansowe",                              razem(r.przychodyFinansowe(), PrzychodyFinansowe::razem));
            add(lista, Sekcja.RZIS, "H",      "Koszty finansowe",                                 razem(r.kosztyFinansowe(), KosztyFinansowe::razem));
            add(lista, Sekcja.RZIS, "I",      "Zysk (strata) brutto",                             r.zyskBrutto());
            add(lista, Sekcja.RZIS, "J",      "Podatek dochodowy",                                r.podatekDochodowy());
            add(lista, Sekcja.RZIS, "K",      "Pozostałe obowiązkowe zmniejszenia zysku (zwiększenia straty)", r.pozostaleObowiazkoweZmniejszeniaZysku());
            add(lista, Sekcja.RZIS, "L",      "Zysk (strata) netto",                              r.zyskNetto());
        }

        return lista;
    }

    public FinanceTable toFinanceTable(List<FinanceData> financeData){

        // oldest first, so newer statements (and corrections) overwrite the same year
        List<FinanceData> sorted = new ArrayList<>(financeData);
        sorted.sort(Comparator.comparing(FinanceData::okresDo)
                .thenComparing(FinanceData::dataSporzadzenia, Comparator.nullsFirst(Comparator.naturalOrder())));

        List<WgraneSprawozdanie> wgraneSprawozdania = new ArrayList<>();
        for (FinanceData fd : sorted){
            WgraneSprawozdanie wgraneSprawozdanie = new FinanceTable.WgraneSprawozdanie(
                    fd.okresDo().getYear(),
                    fd.okresOd(),
                    fd.okresDo(),
                    fd.dataSporzadzenia(),
                    fd.kontynuacjaDzialalnosci()
            );
            wgraneSprawozdania.add(wgraneSprawozdanie);
        }

        LinkedHashMap<String, FinanceRow> financeRows = new LinkedHashMap<>();

        for (FinanceData fd : sorted) {
            int year = fd.okresDo().getYear();

            for (Pozycja p : fd.pozycje()) {
                FinanceRow row = financeRows.computeIfAbsent(p.sekcja() + p.symbol(),
                        k -> new FinanceRow(
                                p.sekcja(),
                                p.symbol(),
                                p.nazwa(),
                                new HashMap<>(),
                                new HashMap<>(),
                                new HashMap<>()
                        )
                );

                if (p.biezacy() != null) row.wartosci().put(year, p.biezacy());
                if (p.poprzedni() != null) row.wartosci().put(year - 1, p.poprzedni());
            }
        }

        // drop rows that are zero in every year
        financeRows.values().removeIf(row -> row.wartosci().values().stream().allMatch(Mapper::isZero));

        Set<Integer> years = new TreeSet<>(Comparator.reverseOrder());
        financeRows.values().forEach(row -> years.addAll(row.wartosci().keySet()));


        for (var val : financeRows.values()){
            for (int y : val.wartosci().keySet()){
                var zmiana = calcZmiana(val.wartosci().get(y), val.wartosci().get(y-1));

                if (zmiana != null){
                    val.zmiana().put(y,zmiana);

                    BigDecimal curr = val.wartosci().get(y);
                    BigDecimal prev = val.wartosci().get(y - 1);
                    Boolean isPos = curr.compareTo(prev) > 0;

                    val.isPositive().put(y, isPos);
                }
            }
        }

        FinanceData newest = sorted.getLast();
        return new FinanceTable(
                newest.nazwaFirmy(),
                newest.krs(),
                newest.nip(),
                new ArrayList<>(years),
                new ArrayList<>(financeRows.values()),
                wgraneSprawozdania
        );

    }


    // Null-safe step into a nested group: returns null when the group is missing in the XML
    private static <T> Kwota razem(T group, Function<T, Kwota> getter) {
        return group == null ? null : getter.apply(group);
    }

    private BigDecimal calcZmiana(BigDecimal curr, BigDecimal prev){
        BigDecimal zmiana;
        if (curr == null || prev == null || isZero(prev)){
            return null;
        }
        var x = curr.subtract(prev).abs();
        var y = prev.abs();
        zmiana = x.multiply(BigDecimal.valueOf(100)).divide(y,2,RoundingMode.HALF_UP);


        return zmiana;
    }

    private void add(List<Pozycja> lista, Sekcja sekcja, String symbol, String nazwa, Kwota kwota) {
        // skip positions missing in the XML or equal to zero in both years
        if (kwota == null) {
            return;
        }
        lista.add(new Pozycja(nazwa, kwota.biezacy(), kwota.poprzedni(), symbol, sekcja));
    }

    // null counts as zero (e.g. KwotaB missing in the first year of activity)
    private static boolean isZero(BigDecimal value) {
        return value == null || value.signum() == 0;
    }


}
