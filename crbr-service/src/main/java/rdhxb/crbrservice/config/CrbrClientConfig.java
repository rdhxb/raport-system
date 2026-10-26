package rdhxb.crbrservice.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import rdhxb.crbrservice.client.CrbrClient;

@Configuration
public class CrbrClientConfig {

    @Bean
    public Jaxb2Marshaller crbrMarshaller(){

        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

        marshaller.setPackagesToScan("pl.gov.mf");

        return marshaller;
    }

    @Bean
    public SaajSoapMessageFactory messageFactory(){

        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        messageFactory.setSoapVersion(SoapVersion.SOAP_12);

        return messageFactory;
    }

    @Bean
    public CrbrClient client(Jaxb2Marshaller marshaller, SaajSoapMessageFactory messageFactory,@Value("${crbr.api.url}") String url){

        CrbrClient client = new CrbrClient();

        client.setDefaultUri(url);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        client.setMessageFactory(messageFactory);

        return client;

    }

}
