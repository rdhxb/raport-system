package rdhxb.financeservice.parser.jednostkainna;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import rdhxb.financeservice.dto.SprawozdanieDto.Metadane;

import static rdhxb.financeservice.util.XmlUtils.exists;
import static rdhxb.financeservice.util.XmlUtils.getAttribute;
import static rdhxb.financeservice.util.XmlUtils.getText;
import static rdhxb.financeservice.util.XmlUtils.isTrue;
import static rdhxb.financeservice.util.XmlUtils.toDate;

@Component
public class MetadaneParser {

    public Metadane parse(Document doc) {
        return new Metadane(
                getTypSprawozdania(doc),
                getAttribute(doc, "KodSprawozdania", "wersjaSchemy"),
                getWariantRzis(doc),
                toDate(getText(doc, "OkresOd")),
                toDate(getText(doc, "OkresDo")),
                toDate(getText(doc, "DataSporzadzenia")),
                getText(doc, "NazwaFirmy"),
                getText(doc, "P_1D"),
                getText(doc, "P_1E"),
                getText(doc, "FormaPrawna"),
                isTrue(getText(doc, "P_5A")) && isTrue(getText(doc, "P_5B"))
        );
    }

    // Local name of the first element inside TrescDokumentu, e.g. "JednostkaInna"
    private String getTypSprawozdania(Document doc) {
        NodeList tresc = doc.getElementsByTagNameNS("*", "TrescDokumentu");
        if (tresc.getLength() == 0) {
            return null;
        }
        // first child is usually a whitespace text node, so look for the first element
        for (Node child = tresc.item(0).getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return child.getLocalName();
            }
        }
        return null;
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
