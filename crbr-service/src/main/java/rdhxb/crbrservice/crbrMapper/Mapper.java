package rdhxb.crbrservice.crbrMapper;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.gov.mf.schematy.ap.apiprzegladowecrbr._2022._12._01.*;
import pl.gov.mf.uslugibiznesowe.uslugiesb.ap.apiprzegladowecrbr._2022._12._01.PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp;
import rdhxb.crbrservice.dto.CrbrApiResponse;
import rdhxb.crbrservice.entity.CrbrModel;
import rdhxb.crbrservice.entity.CrbrPowiazanieModel;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static rdhxb.crbrservice.dto.CrbrApiResponse.*;

@Component
@Slf4j
public class Mapper {

    public CrbrApiResponse toCrbrResponse(PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp apiResponse){

        var res = apiResponse.getPobierzInformacjeOSpolkachIBeneficjentachOdpowiedzDane();
        CrbrApiResponse crbrDto = new CrbrApiResponse(
                res.getIdentyfikatorWniosku(),
                toLocalDateTimeFromXMLCalendar(res.getDataICzasZlozeniaWniosku()),
                toLocalDateTimeFromXMLCalendar(res.getDataICzasUdostepnieniaWniosku()),
                res.getStatus() != null ? StatusCrbr.valueOf(res.getStatus().name()) : null,
                toSpolki(res)

        );

        return crbrDto;
    }
    private List<Spolka> toSpolki(PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzDaneTyp apiResponse){

        if (apiResponse.getListaInformacjiOSpolkachIBeneficjentach() == null) return List.of();

        List<Spolka> listSpolka = new ArrayList<>();

        var spolkaIBeneficjenci = apiResponse.getListaInformacjiOSpolkachIBeneficjentach().getSpolkaIBeneficjenci();

        for (var spolkaIBen : spolkaIBeneficjenci){
            Spolka spolka = new Spolka(
                    spolkaIBen.getNazwa(),
                    spolkaIBen.getNIP(),
                    spolkaIBen.getKRS(),
                    spolkaIBen.getOpisFormyOrganizacyjnej(),
                    toAdres(spolkaIBen),
                    spolkaIBen.getNumerReferencyjny(),
                    toLocalDateFromXMLCalendar(spolkaIBen.getDataPoczatkuPrezentacjiZgloszenia()),
                    toLocalDateFromXMLCalendar(spolkaIBen.getDataKoncaPrezentacjiZgloszenia()),
                    spolkaIBen.getSkorygowane(),
                    spolkaIBen.getPostepowanie(),
                    toListaBeneficjentow(spolkaIBen),
                    toListZglaszajacy(spolkaIBen.getListaZglaszajacych()),
                    toRozbieznoscList(spolkaIBen.getListaInformacjiORozbieznosciach())

            );
            listSpolka.add(spolka);
        }


        return listSpolka;

    }

    private Adres toAdres(SpolkaIBeneficjenciApiZapytaniaTyp spolkaIBen){

            var teryt = spolkaIBen.getTeryt();

            Adres adres = new Adres(
                    spolkaIBen.getUlica(),
                    spolkaIBen.getNrDomu(),
                    spolkaIBen.getNrLokalu(),
                    spolkaIBen.getKodPocztowy(),
                    spolkaIBen.getMiejscowosc(),
                    teryt != null ? teryt.getWojewodztwo() : null,
                    teryt != null ? teryt.getPowiat() : null,
                    teryt != null ? teryt.getGmina() : null,
                    nazwa(spolkaIBen.getKraj())
            );
            return adres;

    }

    private List<Beneficjent> toListaBeneficjentow(SpolkaIBeneficjenciApiZapytaniaTyp spolkaIBen){

        if (spolkaIBen.getListaBeneficjentowRzeczywistych() == null) return List.of();

        var beneficjenci = spolkaIBen.getListaBeneficjentowRzeczywistych().getBeneficjentRzeczywisty();

        List<Beneficjent> listBeneficjenci = new ArrayList<>();
        for (var beneficjent : beneficjenci){
            listBeneficjenci.add(toBeneficjent(beneficjent));
        }
        return listBeneficjenci;
    }

    private Beneficjent toBeneficjent(BeneficjentRzeczywistyTyp benefic){

        List<String> listobywatelstwa = new ArrayList<>();
        var beneObywatelstwa = benefic.getObywatelstwo();

        for (var obywatelstwo: beneObywatelstwa){
            listobywatelstwa.add(nazwa(obywatelstwo));
        }

        Beneficjent beneficjent = new Beneficjent(
                benefic.getPierwszeImie(),
                benefic.getKolejneImiona(),
                benefic.getNazwisko(),
                benefic.getPESEL(),
                toLocalDateFromXMLCalendar(benefic.getDataUrodzenia()),
                listobywatelstwa,
                nazwa(benefic.getKrajZamieszkania()),
                toUdzialList(benefic.getListaInformacjiOUdzialach()),
                benefic.getNazwaBeneficjentaGrupowego(),
                benefic.getInformacjeOUprawnieniachPrzyslugujacychBeneficjentowiGrupowemuTrust()
        );

        return beneficjent;

    }

    private LocalDate toLocalDateFromXMLCalendar(XMLGregorianCalendar gregorianCalendar){
        if (gregorianCalendar == null) return null;

        LocalDate localDate = gregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDate();

        return localDate;
    }

    private LocalDateTime toLocalDateTimeFromXMLCalendar(XMLGregorianCalendar gregorianCalendar){
        if (gregorianCalendar == null) return null;

        LocalDateTime localDateTime = gregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
        return localDateTime;
    }

    private List<Zglaszajacy> toListZglaszajacy(ListaZglaszajacychTyp listaZglaszajacychTyp){
        if (listaZglaszajacychTyp == null) return List.of();

        List<Zglaszajacy> listaZglaszajacych = new ArrayList<>();
        var lista = listaZglaszajacychTyp.getZglaszajacy();

        for (var zglaszajacy : lista){
            Zglaszajacy zgl = new Zglaszajacy(
                    zglaszajacy.getPierwszeImie(),
                    zglaszajacy.getKolejneImiona(),
                    zglaszajacy.getNazwisko(),
                    zglaszajacy.getPESEL(),
                    toLocalDateFromXMLCalendar(zglaszajacy.getDataUrodzenia()),
                    toListFunkcje(zglaszajacy.getListaFunkcjiZglaszajacego()),
                    zglaszajacy.getInneInformacje()
            );
            listaZglaszajacych.add(zgl);

        }
        return listaZglaszajacych;

    }

    private List<String> toListFunkcje(ListaFunkcjiZglaszajacegoTyp listaFunkcjiZglaszajacegoTyp){
        if (listaFunkcjiZglaszajacegoTyp == null) return List.of();

        List<String> listaFunkcji = new ArrayList<>();

        var listaFunkcjiZglaszajacego = listaFunkcjiZglaszajacegoTyp.getFunkcja();

        for (var funkcja : listaFunkcjiZglaszajacego){

            listaFunkcji.add(funkcja.getOpis());

        }
        return listaFunkcji;
    }

    private Udzial toUdzial(InformacjaOUdzialachTyp informacja){

            var bezposrednie = informacja.getUprawnieniaWlascicielskieBezposrednie();
            var uprzywilejowanie = bezposrednie != null ? bezposrednie.getInformacjaOUprzywilejowaniu() : null;
            var inne = informacja.getInneUprawnienia();

            Udzial udzial = new Udzial(
                    bezposrednie != null ? opis(bezposrednie.getUprawnieniaWlascicielskie()) : null,
                    bezposrednie != null ? bezposrednie.getIlosc() : null,
                    bezposrednie != null ? opis(bezposrednie.getJednostkaMiary()) : null,
                    uprzywilejowanie != null ? toRodzajeUprzywilejowaniaRodzajList(uprzywilejowanie.getRodzajUprzywilejowania()) : List.of(),
                    uprzywilejowanie != null ? uprzywilejowanie.getOpisUprzywilejowania() : null,
                    informacja.getUprawnieniaWlascicielskiePosrednie(),
                    inne != null ? opis(inne.getRodzajInnychUprawnien()) : null,
                    inne != null ? inne.getOpisInnychUprawnien() : null
            );

            return udzial;

    }

    private List<Udzial> toUdzialList(ListaInformacjiOUdzialachTyp listaInformacjiOUdzialachTyp){
        if (listaInformacjiOUdzialachTyp == null) return List.of();

        List<Udzial> udzialy = new ArrayList<>();
        var informacjeOUdzialach = listaInformacjiOUdzialachTyp.getInformacjaOUdzialach();

        for (var udzial : informacjeOUdzialach){
            udzialy.add(toUdzial(udzial));
        }
        return udzialy;
    }

    private List<String> toRodzajeUprzywilejowaniaRodzajList(List<TPozycjaSlownika> slownikaList){
        List<String> uprzywilejowaniLista = new ArrayList<>();

        for (var slowo : slownikaList){
            uprzywilejowaniLista.add(opis(slowo));
        }

        return uprzywilejowaniLista;
    }

    private String opis(TPozycjaSlownika slownika){
        return slownika != null ? slownika.getOpis() : null;
    }

    private String nazwa(TKraj kraj){
        return kraj != null ? kraj.getNazwa() : null;
    }

    private Rozbieznosc toRozbieznosc(InformacjaORozbieznosciachTyp informacjaORozbieznosciachTyp){

        Rozbieznosc rozbieznosc = new Rozbieznosc(
                informacjaORozbieznosciachTyp.getIdentyfikatorUwagi(),
                informacjaORozbieznosciachTyp.getInformacjaDlaZainteresowanego()
        );
        return rozbieznosc;

    }

    private List<Rozbieznosc> toRozbieznoscList(ListaInformacjiORozbieznosciachTyp listaInformacjiORozbieznosciachTyp){

        if (listaInformacjiORozbieznosciachTyp == null) return List.of();

        List<Rozbieznosc> rozbieznoscList = new ArrayList<>();

        for (var rozbieznosc : listaInformacjiORozbieznosciachTyp.getInformacjaORozbieznosciach()){
            rozbieznoscList.add(toRozbieznosc(rozbieznosc));
        }
        return rozbieznoscList;

    }


    public CrbrModel toCrbrModel(CrbrApiResponse apiResponse, UUID requestId, String krs){

        CrbrModel crbrModel = new CrbrModel();

        crbrModel.setRequestId(requestId);

        crbrModel.setKrsNumber(krs);

        crbrModel.setStatus(apiResponse.status());
        crbrModel.setIdentyfikatorWniosku(apiResponse.identyfikatorWniosku());
        crbrModel.setFetchedAt(Instant.now());

        return crbrModel;
    }

    public List<CrbrPowiazanieModel> toPowiazania(Spolka spolka, UUID requestId, String krsZrodlowy){

        List<CrbrPowiazanieModel> listaPowiazan = new ArrayList<>();

        for (Beneficjent beneficjent : spolka.beneficjenci()){

            if (beneficjent.pesel() == null || beneficjent.pesel().length() != 11) continue;

            CrbrPowiazanieModel powiazanie = new CrbrPowiazanieModel();

            powiazanie.setRequestId(requestId);

            powiazanie.setPesel(beneficjent.pesel());
            powiazanie.setImie(beneficjent.imie());
            powiazanie.setNazwisko(beneficjent.nazwisko());

            powiazanie.setKrs(spolka.krs());
            powiazanie.setNazwa(spolka.nazwa());
            powiazanie.setNip(spolka.nip());

            if (!beneficjent.udzialy().isEmpty()){
                Udzial udzial = beneficjent.udzialy().get(0);

                powiazanie.setRodzajUprawnien(udzial.rodzajUprawnien());
                powiazanie.setIlosc(udzial.ilosc() != null ? BigDecimal.valueOf(udzial.ilosc()) : null);
                powiazanie.setJednostkaMiary(udzial.jednostkaMiary());
            }

            powiazanie.setZrodlowa(spolka.krs() != null && spolka.krs().equals(krsZrodlowy));

            listaPowiazan.add(powiazanie);
        }

        return listaPowiazan;
    }


}
