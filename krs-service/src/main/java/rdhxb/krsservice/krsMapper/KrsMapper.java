package rdhxb.krsservice.krsMapper;

import org.springframework.stereotype.Component;
import rdhxb.common.krs.*;
import rdhxb.krsservice.dto.KrsApiResponse;
import rdhxb.krsservice.dto.KrsApiResponse.Dzial1;
import rdhxb.krsservice.dto.KrsApiResponse.Dzial2;
import rdhxb.krsservice.dto.KrsApiResponse.Dzial3;
import rdhxb.krsservice.entity.KrsModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

@Component
public class KrsMapper {

    private static final DateTimeFormatter KRS_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public KrsModel toKrsModel(KrsApiResponse krsApiResponse, UUID requestId){

        var naglowek = krsApiResponse.odpis().naglowekA();
        Dzial1 dzial1 = krsApiResponse.odpis().dane().dzial1();
        Dzial2 dzial2 = krsApiResponse.odpis().dane().dzial2();
        Dzial3 dzial3 = krsApiResponse.odpis().dane().dzial3();

        var podmiot = dzial1.danePodmiotu();
        var siedzibaIAdres = dzial1.siedzibaIAdres();



        KrsModel krsModel = new KrsModel();

        krsModel.setRequestId(requestId);
        krsModel.setFetchedAt(Instant.now());

//        --- naglowek ---
        krsModel.setKrsNumber(naglowek.numerKRS());
        krsModel.setRejestr(naglowek.rejestr());
        krsModel.setStanZDnia(parseDate(naglowek.stanZDnia()));
        krsModel.setDataRejestracji(parseDate(naglowek.dataRejestracjiWKRS()));
        krsModel.setNumerOstatniegoWpisu(naglowek.numerOstatniegoWpisu());
        krsModel.setDataOstatniegoWpisu(parseDate(naglowek.dataOstatniegoWpisu()));

//        --- dane podmiotu ---
        if (podmiot != null){
            krsModel.setNazwa(podmiot.nazwa());
            krsModel.setFormaPrawna(podmiot.formaPrawna());
            krsModel.setStatusOpp(podmiot.czyPosiadaStatusOPP());

            if (podmiot.identyfikatory() != null){
                krsModel.setNip(podmiot.identyfikatory().nip());
                krsModel.setRegon(podmiot.identyfikatory().regon());
            }
        }

//        --- adres ---
        if (siedzibaIAdres != null){
            krsModel.setUlica(siedzibaIAdres.adres().ulica());
            krsModel.setNrDomu(siedzibaIAdres.adres().nrDomu());
            krsModel.setNrLokalu(siedzibaIAdres.adres().nrLokalu());
            krsModel.setMiejscowosc(siedzibaIAdres.adres().miejscowosc());
            krsModel.setKodPocztowy(siedzibaIAdres.adres().kodPocztowy());
            krsModel.setPoczta(siedzibaIAdres.adres().poczta());
            krsModel.setKraj(siedzibaIAdres.adres().kraj());

            krsModel.setKrajSiedziby(siedzibaIAdres.siedziba().kraj());
            krsModel.setWojewodztwo(siedzibaIAdres.siedziba().wojewodztwo());
            krsModel.setPowiat(siedzibaIAdres.siedziba().powiat());
            krsModel.setGmina(siedzibaIAdres.siedziba().gmina());
            krsModel.setMiejscowoscSiedziby(siedzibaIAdres.siedziba().miejscowosc());

        }

//        --- kapital ---
        if (dzial1.kapital() != null && dzial1.kapital().wysokoscKapitaluZakladowego() != null){
            var kwota = dzial1.kapital().wysokoscKapitaluZakladowego();
            krsModel.setKapitalZakladowy(parseKwota(kwota.wartosc()));
            krsModel.setKapitalWaluta(kwota.waluta());
        }
        // absent for S.A., associations, foundations
        krsModel.setWspolnicySpzoo(toWspolnikData(dzial1.wspolnicySpzoo()));

//        --- reprezentacja ---
        if (dzial2 != null && dzial2.reprezentacja() != null){
            krsModel.setOrganReprezentacji(dzial2.reprezentacja().nazwaOrganu());
            krsModel.setSposobReprezentacji(dzial2.reprezentacja().sposobReprezentacji());
            krsModel.setSklad(toCzlonekOrganuData(dzial2.reprezentacja().sklad()));
        }

//        --- przedmiot dzialalnosci ---
        if (dzial3 != null && dzial3.przedmiotDzialalnosci() != null){
            var przedmiot = dzial3.przedmiotDzialalnosci();
            krsModel.setPrzedmiotPrzewazajacejDzialalnosci(toPkdData(przedmiot.przedmiotPrzewazajacejDzialalnosci()));
            krsModel.setPrzedmiotPozostalejDzialalnosci(toPkdData(przedmiot.przedmiotPozostalejDzialalnosci()));
        }

//        --- zlozone dokumenty ---
        if (dzial3 != null && dzial3.wzmiankiOZlozonychDokumentach() != null){
            krsModel.setWzmiankaOZlozeniuRocznegoSprawozdaniaFinansowego(
                    toSprawozdanieFinansoweData(dzial3.wzmiankiOZlozonychDokumentach().wzmiankaOZlozeniuRocznegoSprawozdaniaFinansowego()));
        }

        return krsModel;
    }

    public KrsData toKrsData(KrsModel krsModel){

        return new KrsData(
//                --- naglowek ---
                krsModel.getKrsNumber(),
                krsModel.getRejestr(),
                krsModel.getStanZDnia(),
                krsModel.getDataRejestracji(),
                krsModel.getNumerOstatniegoWpisu(),
                krsModel.getDataOstatniegoWpisu(),

//                --- dane podmiotu ---
                krsModel.getNazwa(),
                krsModel.getFormaPrawna(),
                krsModel.getNip(),
                krsModel.getRegon(),
                krsModel.getStatusOpp(),

//                --- adres ---
                krsModel.getUlica(),
                krsModel.getNrDomu(),
                krsModel.getNrLokalu(),
                krsModel.getMiejscowosc(),
                krsModel.getKodPocztowy(),
                krsModel.getPoczta(),
                krsModel.getKraj(),

//                --- siedziba ---
                krsModel.getKrajSiedziby(),
                krsModel.getWojewodztwo(),
                krsModel.getPowiat(),
                krsModel.getGmina(),
                krsModel.getMiejscowoscSiedziby(),

                krsModel.getEmail(),
                krsModel.getWww(),

//                --- kapital ---
                krsModel.getKapitalZakladowy(),
                krsModel.getKapitalWaluta(),

//                --- reprezentacja ---
                krsModel.getOrganReprezentacji(),
                krsModel.getSposobReprezentacji(),
                krsModel.getSklad(),

                krsModel.getPrzedmiotPrzewazajacejDzialalnosci(),
                krsModel.getPrzedmiotPozostalejDzialalnosci(),

                krsModel.getWzmiankaOZlozeniuRocznegoSprawozdaniaFinansowego(),

                krsModel.getWspolnicySpzoo()

        );
    }

    private LocalDate parseDate(String date){
        if (date == null || date.isBlank()){
            return null;
        }
        return LocalDate.parse(date, KRS_DATE);
    }

    private BigDecimal parseKwota(String wartosc){
        if (wartosc == null || wartosc.isBlank()){
            return null;
        }
        return new BigDecimal(wartosc.replace(" ", "").replace(".", "").replace(",", "."));
    }

    private List<PkdData> toPkdData(List<KrsApiResponse.Pkd> pkds){
        if (pkds == null) return List.of();
        List<PkdData> pkdDataList = new ArrayList<>();
        for (KrsApiResponse.Pkd pkd : pkds){
            PkdData pkdData = new PkdData(
                    pkd.opis(),
                    pkd.kodDzial(),
                    pkd.kodKlasa(),
                    pkd.kodPodklasa()
            );
            pkdDataList.add(pkdData);
        }

        return pkdDataList;
    }

    private List<SprawozdanieFinansoweData> toSprawozdanieFinansoweData(List<KrsApiResponse.SprawozdanieFinansowe> sprawozdaniaFinansowe){
        if (sprawozdaniaFinansowe == null) return List.of();

        List<SprawozdanieFinansoweData> finansoweDataList = new ArrayList<>();

        for (KrsApiResponse.SprawozdanieFinansowe sprawozdanie : sprawozdaniaFinansowe){
            SprawozdanieFinansoweData sprawozdanieFinansoweData = new SprawozdanieFinansoweData(
                    sprawozdanie.dataZlozenia(),
                    sprawozdanie.zaOkresOdDo()
            );
            finansoweDataList.add(sprawozdanieFinansoweData);
        }
        return finansoweDataList;

    }

    private List<CzlonekOrganuData> toCzlonekOrganuData(List<KrsApiResponse.CzlonekOrganu> czlonekowieOrganu){
        if (czlonekowieOrganu == null) return List.of();
        List<CzlonekOrganuData> czlonekOrganuDataList = new ArrayList<>();
        for (KrsApiResponse.CzlonekOrganu czlonek : czlonekowieOrganu){
            CzlonekOrganuData czlonekOrganuData = new CzlonekOrganuData(
                    imie(czlonek.imiona()),
                    nazwisko(czlonek.nazwisko()),
                    czlonek.funkcjaWOrganie(),
                    czlonek.czyZawieszona()
            );
            czlonekOrganuDataList.add(czlonekOrganuData);
        }
        return czlonekOrganuDataList;
    }

    private List<WspolnikData> toWspolnikData(List<KrsApiResponse.Wspolnik> wspolnicy){
        if (wspolnicy == null) return List.of();
        List<WspolnikData> wspolnikDataList = new ArrayList<>();

        for (KrsApiResponse.Wspolnik wspolnik : wspolnicy){
            WspolnikData wspolnikData = new WspolnikData(
                    wspolnikNazwa(wspolnik),
                    wspolnik.identyfikator() != null ? wspolnik.identyfikator().nip() : null,
                    wspolnik.krs() != null ? wspolnik.krs().krs() : null,
                    wspolnik.posiadaneUdzialy(),
                    wspolnik.czyPosiadaCaloscUdzialow()
            );
            wspolnikDataList.add(wspolnikData);
        }
        return wspolnikDataList;

    }

    private String imie(KrsApiResponse.Imiona imiona){
        return imiona != null ? imiona.imie() : null;
    }

    private String nazwisko(KrsApiResponse.Nazwisko nazwisko){
        return nazwisko != null ? nazwisko.nazwiskoICzlon() : null;
    }

    // Companies have "nazwa", natural persons come with nazwisko + imiona instead
    private String wspolnikNazwa(KrsApiResponse.Wspolnik wspolnik){
        if (wspolnik.nazwa() != null && !wspolnik.nazwa().isBlank()){
            return wspolnik.nazwa();
        }
        String imie = imie(wspolnik.imiona());
        String nazwisko = nazwisko(wspolnik.nazwisko());
        if (imie == null) return nazwisko;
        if (nazwisko == null) return imie;
        return imie + " " + nazwisko;
    }

}
