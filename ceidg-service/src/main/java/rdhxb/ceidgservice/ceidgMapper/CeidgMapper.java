package rdhxb.ceidgservice.ceidgMapper;

import org.springframework.stereotype.Component;
import rdhxb.ceidgservice.dto.CeidgApiResponse;
import rdhxb.ceidgservice.dto.CeidgApiResponse.Adres;
import rdhxb.ceidgservice.dto.CeidgApiResponse.Firma;
import rdhxb.ceidgservice.dto.CeidgApiResponse.Obywatelstwo;
import rdhxb.ceidgservice.dto.CeidgApiResponse.Pkd;
import rdhxb.ceidgservice.entity.CeidgModel;
import rdhxb.common.ceidg.CeidgHistoriaData;
import rdhxb.common.ceidg.CeidgPkdData;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CeidgMapper {

    private static final String WYKRESLONY = "WYKRESLONY";

    public CeidgModel toCeidgModel(CeidgApiResponse ceidgApiResponse, UUID requestId){

        List<Firma> firmy = ceidgApiResponse.firma() == null ? List.of() : ceidgApiResponse.firma();

        Firma aktualna = null;
        for (Firma firma : firmy){
            if (WYKRESLONY.equals(firma.status())){
                continue;
            }
            if (aktualna == null || jestNowsza(firma, aktualna)){
                aktualna = firma;
            }
        }

        CeidgModel ceidgModel = new CeidgModel();
        ceidgModel.setRequestId(requestId);
        ceidgModel.setFetchedAt(Instant.now());
        ceidgModel.setMaAktualnaFirme(aktualna != null);

        // NIP belongs to the owner, so it is the same in every entry
        for (Firma firma : firmy){
            if (firma.wlasciciel() != null && firma.wlasciciel().nip() != null){
                ceidgModel.setNip(firma.wlasciciel().nip());
                break;
            }
        }

        if (aktualna != null){
            mapAktualnaFirma(ceidgModel, aktualna);
        }

        // --- historia: every entry except the current one, newest first ---
        List<Firma> poprzednie = new ArrayList<>();
        for (Firma firma : firmy){
            if (firma != aktualna){
                poprzednie.add(firma);
            }
        }
        poprzednie.sort((a, b) -> jestNowsza(a, b) ? -1 : jestNowsza(b, a) ? 1 : 0);

        List<CeidgHistoriaData> historia = new ArrayList<>();
        for (Firma firma : poprzednie){
            historia.add(toHistoria(firma));
        }
        ceidgModel.setHistoria(historia);

        return ceidgModel;
    }

    private void mapAktualnaFirma(CeidgModel ceidgModel, Firma firma){

        ceidgModel.setCeidgId(firma.id());
        ceidgModel.setNazwa(trim(firma.nazwa()));
        ceidgModel.setStatus(firma.status());
        ceidgModel.setNumerStatusu(firma.numerStatusu());

//        --- wlasciciel ---
        CeidgApiResponse.Wlasciciel wlasciciel = firma.wlasciciel();
        if (wlasciciel != null){
            ceidgModel.setImie(wlasciciel.imie());
            ceidgModel.setNazwisko(wlasciciel.nazwisko());
            ceidgModel.setRegon(blankToNull(wlasciciel.regon()));
        }

        if (firma.obywatelstwa() != null){
            List<String> obywatelstwa = new ArrayList<>();
            for (Obywatelstwo obywatelstwo : firma.obywatelstwa()){
                if (obywatelstwo.symbol() != null){
                    obywatelstwa.add(obywatelstwo.symbol());
                }
            }
            ceidgModel.setObywatelstwa(obywatelstwa);
        }

//        --- daty ---
        ceidgModel.setDataRozpoczecia(firma.dataRozpoczecia());
        ceidgModel.setDataZawieszenia(firma.dataZawieszenia());
        ceidgModel.setDataWznowienia(firma.dataWznowienia());
        ceidgModel.setDataZakonczenia(firma.dataZakonczenia());
        ceidgModel.setDataWykreslenia(firma.dataWykreslenia());

//        --- adres dzialalnosci ---
        Adres adres = firma.adresDzialalnosci();
        if (adres != null){
            ceidgModel.setUlica(trim(adres.ulica()));
            ceidgModel.setBudynek(adres.budynek());
            ceidgModel.setLokal(adres.lokal());
            ceidgModel.setMiasto(adres.miasto());
            ceidgModel.setKodPocztowy(adres.kod());
            ceidgModel.setWojewodztwo(adres.wojewodztwo());
            ceidgModel.setPowiat(adres.powiat());
            ceidgModel.setGmina(adres.gmina());
            ceidgModel.setKraj(adres.kraj());
        }

//        --- adres korespondencyjny ---
        ceidgModel.setAdresKorespondencyjny(formatAdres(firma.adresKorespondencyjny()));

//        --- kontakt ---
        ceidgModel.setTelefon(firma.telefon());
        ceidgModel.setEmail(firma.email());
        ceidgModel.setWww(firma.www());

        ceidgModel.setWspolnoscMajatkowa(firma.wspolnoscMajatkowa());

//        --- PKD ---
        ceidgModel.setRokPkd(firma.rokPkd());
        ceidgModel.setPkdGlowny(toPkd(firma.pkdGlowny()));
        if (firma.pkd() != null){
            List<CeidgPkdData> pkdList = new ArrayList<>();
            for (Pkd pkd : firma.pkd()){
                if (pkd != null){
                    pkdList.add(toPkd(pkd));
                }
            }
            ceidgModel.setPkd(pkdList);
        }
    }

    // true when "a" started later than "b"; entry without start date always loses
    private boolean jestNowsza(Firma a, Firma b){
        LocalDate dataA = a.dataRozpoczecia();
        LocalDate dataB = b.dataRozpoczecia();
        if (dataA == null){
            return false;
        }
        if (dataB == null){
            return true;
        }
        return dataA.isAfter(dataB);
    }

    private CeidgHistoriaData toHistoria(Firma firma){
        return new CeidgHistoriaData(
                firma.id(),
                trim(firma.nazwa()),
                firma.status(),
                firma.dataRozpoczecia(),
                firma.dataZakonczenia(),
                firma.dataWykreslenia()
        );
    }

    private CeidgPkdData toPkd(Pkd pkd){
        if (pkd == null){
            return null;
        }
        return new CeidgPkdData(pkd.kod(), pkd.nazwa());
    }

    private String formatAdres(Adres adres){
        if (adres == null){
            return null;
        }

        String numer = join("/", adres.budynek(), adres.lokal());
        String ulicaINumer = join(" ", adres.ulica(), numer);
        String kodIMiasto = join(" ", adres.kod(), adres.miasto());
        String wynik = join(", ", adres.adresat(), ulicaINumer, kodIMiasto);

        return wynik.isBlank() ? null : wynik;
    }

    // joins non-blank parts with separator, skipping null/empty ones
    private String join(String separator, String... parts){
        StringBuilder sb = new StringBuilder();
        for (String part : parts){
            if (part == null || part.isBlank()){
                continue;
            }
            if (!sb.isEmpty()){
                sb.append(separator);
            }
            sb.append(part.trim());
        }
        return sb.toString();
    }

    private String trim(String value){
        return value == null ? null : value.trim();
    }

    private String blankToNull(String value){
        return value == null || value.isBlank() ? null : value;
    }
}
