package rdhxb.financeservice.parser.jednostkainna;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rdhxb.financeservice.dto.SprawozdanieDto.*;

import static rdhxb.financeservice.util.XmlUtils.findElement;
import static rdhxb.financeservice.util.XmlUtils.getChild;
import static rdhxb.financeservice.util.XmlUtils.getKwota;

// Every group method returns null when its element is missing in the XML
@Component
public class RachunekZyskowIStratParser {

    // RZiS positions are single letters (A, B, C ...), so only direct children of RZiSPor are read
    public RachunekZyskowIStrat parse(Document doc, String wariantRzis) {
        if (!"RZiSPor".equals(wariantRzis)) {
            return null; // RZiSKalk uses different letters - not supported yet
        }
        Element rzis = findElement(doc.getDocumentElement(), "RZiSPor");

        return new RachunekZyskowIStrat(
                przychodyNetto(getChild(rzis, "A")),
                koszty(getChild(rzis, "B")),
                getKwota(getChild(rzis, "C")),
                pozostalePrzychody(getChild(rzis, "D")),
                pozostaleKoszty(getChild(rzis, "E")),
                getKwota(getChild(rzis, "F")),
                przychodyFinansowe(getChild(rzis, "G")),
                kosztyFinansowe(getChild(rzis, "H")),
                getKwota(getChild(rzis, "I")),
                getKwota(getChild(rzis, "J")),
                getKwota(getChild(rzis, "K")),
                getKwota(getChild(rzis, "L"))
        );
    }

    private PrzychodyNetto przychodyNetto(Element e) {
        if (e == null) return null;
        return new PrzychodyNetto(getKwota(e), k(e, "J"), k(e, "I"), k(e, "II"), k(e, "III"), k(e, "IV"));
    }

    private KosztyDzialalnosciOperacyjnej koszty(Element e) {
        if (e == null) return null;
        return new KosztyDzialalnosciOperacyjnej(
                getKwota(e),
                k(e, "I"),
                k(e, "II"),
                k(e, "III"),
                wTym(c(e, "IV"), "1"),
                k(e, "V"),
                wTym(c(e, "VI"), "1"),
                k(e, "VII"),
                k(e, "VIII")
        );
    }

    private PozostalePrzychodyOperacyjne pozostalePrzychody(Element e) {
        if (e == null) return null;
        return new PozostalePrzychodyOperacyjne(getKwota(e), k(e, "I"), k(e, "II"), k(e, "III"), k(e, "IV"));
    }

    private PozostaleKosztyOperacyjne pozostaleKoszty(Element e) {
        if (e == null) return null;
        return new PozostaleKosztyOperacyjne(getKwota(e), k(e, "I"), k(e, "II"), k(e, "III"));
    }

    private PrzychodyFinansowe przychodyFinansowe(Element e) {
        if (e == null) return null;
        Element dywidendy = c(e, "I");
        return new PrzychodyFinansowe(
                getKwota(e),
                dywidendy == null ? null : new Dywidendy(
                        getKwota(dywidendy),
                        wTym(c(dywidendy, "A"), "1"),
                        wTym(c(dywidendy, "B"), "1")
                ),
                wTym(c(e, "II"), "J"),
                wTym(c(e, "III"), "J"),
                k(e, "IV"),
                k(e, "V")
        );
    }

    private KosztyFinansowe kosztyFinansowe(Element e) {
        if (e == null) return null;
        return new KosztyFinansowe(
                getKwota(e),
                wTym(c(e, "I"), "J"),
                wTym(c(e, "II"), "J"),
                k(e, "III"),
                k(e, "IV")
        );
    }

    // Position with its "w tym" child, e.g. wTym(G_II, "J") -> G_II + G_II_J
    private KwotaWTym wTym(Element e, String suffix) {
        if (e == null) return null;
        return new KwotaWTym(getKwota(e), k(e, suffix));
    }

    // Child symbol = parent symbol + "_" + suffix, e.g. c(B, "IV") -> B_IV
    private static Element c(Element parent, String suffix) {
        return parent == null ? null : getChild(parent, parent.getLocalName() + "_" + suffix);
    }

    private static Kwota k(Element parent, String suffix) {
        return getKwota(c(parent, suffix));
    }
}
