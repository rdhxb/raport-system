package rdhxb.financeservice.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import rdhxb.financeservice.dto.SprawozdanieDto.Sprawozdanie;
import rdhxb.financeservice.parser.jednostkainna.JednostkaInnaParser;
import rdhxb.financeservice.parser.jednostkamala.JednostkaMalaParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;

import static rdhxb.financeservice.util.XmlUtils.getChild;


// Loads the XML and hands it to the parser for its statement type
@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentParser {

    private final JednostkaInnaParser jednostkaInnaParser;
    private final JednostkaMalaParser jednostkaMalaParser;

    public Sprawozdanie parseDocument(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(inputStream);

        String typSprawozdania = getTypSprawozdania(doc);
        log.info("Statement type: {}", typSprawozdania);

        if (typSprawozdania == null) {
            throw new IllegalArgumentException("Unknown statement type");
        }

        return switch (typSprawozdania) {
            case "JednostkaInna" -> jednostkaInnaParser.parse(doc);
            case "JednostkaMala" -> jednostkaMalaParser.parse(doc);
            default -> throw new IllegalArgumentException("Unsupported statement type: " + typSprawozdania);
        };
    }

    // JednostkaInna wraps the statement in Dokument/TrescDokumentu, JednostkaMala has it as the root element
    private String getTypSprawozdania(Document doc) {
        Element root = doc.getDocumentElement();
        if (!"Dokument".equals(root.getLocalName())) {
            return root.getLocalName();
        }
        Element tresc = getChild(root, "TrescDokumentu");
        if (tresc == null) {
            return null;
        }
        for (Node child = tresc.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return child.getLocalName();
            }
        }
        return null;
    }
}
