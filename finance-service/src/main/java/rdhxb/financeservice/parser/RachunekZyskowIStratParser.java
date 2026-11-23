package rdhxb.financeservice.parser;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rdhxb.financeservice.dto.SprawozdanieDto.RachunekZyskowIStrat;

import static rdhxb.financeservice.util.XmlUtils.findElement;
import static rdhxb.financeservice.util.XmlUtils.getChild;
import static rdhxb.financeservice.util.XmlUtils.getKwota;

@Component
public class RachunekZyskowIStratParser {

    // RZiS positions are single letters (A, B, C ...), so only direct children of RZiSPor are read
    public RachunekZyskowIStrat parse(Document doc, String wariantRzis) {
        if (!"RZiSPor".equals(wariantRzis)) {
            return null; // RZiSKalk uses different letters - not supported yet
        }
        Element rzis = findElement(doc.getDocumentElement(), "RZiSPor");
        Element koszty = getChild(rzis, "B");
        Element kosztyFinansowe = getChild(rzis, "H");

        return new RachunekZyskowIStrat(
                getKwota(getChild(rzis, "A")),
                getKwota(koszty),
                getKwota(getChild(koszty, "B_I")),
                getKwota(getChild(koszty, "B_V")),
                getKwota(getChild(rzis, "C")),
                getKwota(getChild(rzis, "D")),
                getKwota(getChild(rzis, "E")),
                getKwota(getChild(rzis, "F")),
                getKwota(getChild(rzis, "G")),
                getKwota(kosztyFinansowe),
                getKwota(getChild(kosztyFinansowe, "H_I")),
                getKwota(getChild(rzis, "I")),
                getKwota(getChild(rzis, "J")),
                getKwota(getChild(rzis, "L"))
        );
    }
}
