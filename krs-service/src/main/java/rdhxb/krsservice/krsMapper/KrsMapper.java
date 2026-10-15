package rdhxb.krsservice.krsMapper;

import org.springframework.stereotype.Component;
import rdhxb.common.model.KrsData;
import rdhxb.krsservice.dto.KrsApiResponse;
import rdhxb.krsservice.model.KrsModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class KrsMapper {

    private static final DateTimeFormatter KRS_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public KrsModel toKrsModel(KrsApiResponse krsApiResponse, UUID requestId){

        var naglowek = krsApiResponse.odpis().naglowekA();
        var dzial1 = krsApiResponse.odpis().dane().dzial1();
        var dzial2 = krsApiResponse.odpis().dane().dzial2();

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

//        --- reprezentacja ---
        if (dzial2 != null && dzial2.reprezentacja() != null){
            krsModel.setOrganReprezentacji(dzial2.reprezentacja().nazwaOrganu());
            krsModel.setSposobReprezentacji(dzial2.reprezentacja().sposobReprezentacji());
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
                krsModel.getSposobReprezentacji()
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

}
