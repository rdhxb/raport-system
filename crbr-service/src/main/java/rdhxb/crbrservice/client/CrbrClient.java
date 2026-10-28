package rdhxb.crbrservice.client;

import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.WebServiceException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import pl.gov.mf.schematy.ap.apiprzegladowecrbr._2022._12._01.PobierzInformacjeOSpolkachIBeneficjentachDaneTyp;
import pl.gov.mf.schematy.ap.apiprzegladowecrbr._2022._12._01.SzczegolyWnioskuTyp;
import pl.gov.mf.uslugibiznesowe.uslugiesb.ap.apiprzegladowecrbr._2022._12._01.ObjectFactory;
import pl.gov.mf.uslugibiznesowe.uslugiesb.ap.apiprzegladowecrbr._2022._12._01.PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp;
import pl.gov.mf.uslugibiznesowe.uslugiesb.ap.apiprzegladowecrbr._2022._12._01.PobierzInformacjeOSpolkachIBeneficjentachTyp;

@Slf4j
public class CrbrClient extends WebServiceGatewaySupport {

    private static final String SOAP_ACTION =
            "http://www.mf.gov.pl/uslugiBiznesowe/uslugiESB/AP/ApiPrzegladoweCRBR/2022/12/01/PobierzInformacjeOSpolkachIBeneficjentach";

    private final ObjectFactory objectFactory = new ObjectFactory();

    @SuppressWarnings("unchecked")
    private PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp send(SzczegolyWnioskuTyp szczegoly, String identyfikator){

        PobierzInformacjeOSpolkachIBeneficjentachDaneTyp dane = new PobierzInformacjeOSpolkachIBeneficjentachDaneTyp();
        dane.setSzczegolyWniosku(szczegoly);

        PobierzInformacjeOSpolkachIBeneficjentachTyp typ = new PobierzInformacjeOSpolkachIBeneficjentachTyp();
        typ.setPobierzInformacjeOSpolkachIBeneficjentachDane(dane);

        JAXBElement<PobierzInformacjeOSpolkachIBeneficjentachTyp> request =
                objectFactory.createPobierzInformacjeOSpolkachIBeneficjentach(typ);

        JAXBElement<PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp> response;
        try {
            response = (JAXBElement<PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp>)
                    getWebServiceTemplate().marshalSendAndReceive(request, new SoapActionCallback(SOAP_ACTION));
        } catch (SoapFaultClientException e) {
            log.error("CRBR API SOAP fault dla : {} -> {}", identyfikator, e.getFaultStringOrReason(), e);
            throw e;
        } catch (WebServiceException e) {
            log.error("CRBR API blad polaczenia dla : {} -> {}", identyfikator, e.getMessage(), e);
            throw e;
        }

        if (response == null || response.getValue() == null) {
            log.error("CRBR API pusta odpowiedz dla : {}", identyfikator);
            throw new IllegalStateException("CRBR API pusta odpowiedz dla : " + identyfikator);
        }

        return response.getValue();

    }

    public PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp getDataFromKrs(String krsNumber) {

        SzczegolyWnioskuTyp szczegoly = new SzczegolyWnioskuTyp();
        szczegoly.setKRS(krsNumber);

        return send(szczegoly, krsNumber);
    }

    public PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp getDataFromPesel(String pesel){

        SzczegolyWnioskuTyp szczegoly = new SzczegolyWnioskuTyp();
        szczegoly.setPESEL(pesel);

        return send(szczegoly, mask(pesel));

    }

    public static String mask(String pesel){
        if (pesel == null || pesel.length() != 11){
            return "invalid";
        }

        int len = pesel.length();

        String maskedPesel = pesel.substring(0,5);
        maskedPesel = maskedPesel + "*".repeat(len - 5);

        return maskedPesel;
    }

}
