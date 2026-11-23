package rdhxb.financeservice.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import rdhxb.financeservice.dto.SprawozdanieDto.Bilans;
import rdhxb.financeservice.dto.SprawozdanieDto.Metadane;
import rdhxb.financeservice.dto.SprawozdanieDto.RachunekZyskowIStrat;
import rdhxb.financeservice.dto.SprawozdanieDto.Sprawozdanie;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;


// Loads the XML and assembles the full Sprawozdanie from the section parsers
@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentParser {

    private final MetadaneParser metadaneParser;
    private final BilansParser bilansParser;
    private final RachunekZyskowIStratParser rachunekZyskowIStratParser;

    public Sprawozdanie parseDocument(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);


        Metadane metadata = metadaneParser.parse(doc);
        Bilans bilans = bilansParser.parse(doc);
        RachunekZyskowIStrat rzis = rachunekZyskowIStratParser.parse(doc, metadata.wariantRzis());

        return new Sprawozdanie(
                metadata,
                bilans,
                rzis
        );

    }
}
