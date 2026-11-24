package rdhxb.financeservice.parser.jednostkamala;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import rdhxb.financeservice.dto.SprawozdanieDto.Metadane;

import static rdhxb.financeservice.util.XmlUtils.exists;
import static rdhxb.financeservice.util.XmlUtils.getAttribute;
import static rdhxb.financeservice.util.XmlUtils.getText;
import static rdhxb.financeservice.util.XmlUtils.isTrue;
import static rdhxb.financeservice.util.XmlUtils.toDate;

@Component
public class MetadaneMalaParser {

    public Metadane parse(Document doc) {
        return new Metadane(
                doc.getDocumentElement().getLocalName(),
                getAttribute(doc, "KodSprawozdania", "wersjaSchemy"),
                getWariantRzis(doc),
                toDate(getText(doc, "OkresOd")),
                toDate(getText(doc, "OkresDo")),
                toDate(getText(doc, "DataSporzadzenia")),
                getText(doc, "NazwaFirmy"),
                getText(doc, "P_1C"),
                getText(doc, "P_1D"),
                null, // JednostkaMala has no FormaPrawna
                isTrue(getText(doc, "P_5A")) && isTrue(getText(doc, "P_5B"))
        );
    }

    private String getWariantRzis(Document doc) {
        if (exists(doc, "RZiSPor")) {
            return "RZiSPor";
        }
        if (exists(doc, "RZiSKalk")) {
            return "RZiSKalk";
        }
        return null;
    }
}
