package rdhxb.crbrservice.client;

import jakarta.xml.bind.JAXBElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.ws.WebServiceException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import pl.gov.mf.schematy.ap.apiprzegladowecrbr._2022._12._01.PobierzInformacjeOSpolkachIBeneficjentachDaneTyp;
import pl.gov.mf.schematy.ap.apiprzegladowecrbr._2022._12._01.SzczegolyWnioskuTyp;
import pl.gov.mf.uslugibiznesowe.uslugiesb.ap.apiprzegladowecrbr._2022._12._01.ObjectFactory;
import pl.gov.mf.uslugibiznesowe.uslugiesb.ap.apiprzegladowecrbr._2022._12._01.PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp;
import pl.gov.mf.uslugibiznesowe.uslugiesb.ap.apiprzegladowecrbr._2022._12._01.PobierzInformacjeOSpolkachIBeneficjentachTyp;
import rdhxb.common.exception.PermanentApiException;
import rdhxb.common.exception.TransientApiException;

import static rdhxb.common.base.PeselMasker.mask;

@Slf4j
public class CrbrClient extends WebServiceGatewaySupport {

    private static final String SOAP_ACTION =
            "http://www.mf.gov.pl/uslugiBiznesowe/uslugiESB/AP/ApiPrzegladoweCRBR/2022/12/01/PobierzInformacjeOSpolkachIBeneficjentach";

    private final ObjectFactory objectFactory = new ObjectFactory();

    @SuppressWarnings("unchecked")
    private PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp send(SzczegolyWnioskuTyp szczegoly, String identyfikator) throws PermanentApiException {

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
            String eMess = "CRBR API SOAP fault dla : " + identyfikator + " -> " +  e.getFaultStringOrReason();
            throw new PermanentApiException(eMess);

        } catch (WebServiceIOException e) {
            // Timeout, connection refused, unknown host, HTTP 5xx (WebServiceTransportException) – worth retrying
            log.error("CRBR API blad polaczenia dla : {} -> {}", identyfikator, e.getMessage(), e);
            String eMess = "CRBR API blad polaczenia dla : " + identyfikator + " -> " +  e.getMessage();
            throw new TransientApiException(eMess, e);

        } catch (WebServiceException e) {
            // Any other Spring-WS error (e.g. building the SOAP message) – retrying won't help
            log.error("CRBR API blad dla : {} -> {}", identyfikator, e.getMessage(), e);
            String eMess = "CRBR API blad dla : " + identyfikator + " -> " +  e.getMessage();
            throw new PermanentApiException(eMess);
        } catch (UnmarshallingFailureException e){
            // Response XML doesn't match the schema – retrying won't help
            log.error("CRBR API zly XML dla : {} -> {}", identyfikator, e.getMessage(), e);
            String eMess = "CRBR API zly XML dla : " + identyfikator + " -> " + e.getMessage();
            throw new PermanentApiException(eMess);
        }

        if (response == null || response.getValue() == null) {
            log.error("CRBR API pusta odpowiedz dla : {}", identyfikator);
            String eMess = "CRBR API pusta odpowiedz dla : " + identyfikator;
            throw new PermanentApiException(eMess);
        }


        return response.getValue();

    }

    public PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp getDataFromKrs(String krsNumber) throws PermanentApiException {

        SzczegolyWnioskuTyp szczegoly = new SzczegolyWnioskuTyp();
        szczegoly.setKRS(krsNumber);

        return send(szczegoly, krsNumber);
    }

    public PobierzInformacjeOSpolkachIBeneficjentachOdpowiedzTyp getDataFromPesel(String pesel) throws PermanentApiException {

        SzczegolyWnioskuTyp szczegoly = new SzczegolyWnioskuTyp();
        szczegoly.setPESEL(pesel);

        return send(szczegoly, mask(pesel));

    }

}
