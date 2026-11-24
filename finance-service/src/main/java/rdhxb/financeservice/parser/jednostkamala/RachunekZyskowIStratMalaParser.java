package rdhxb.financeservice.parser.jednostkamala;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rdhxb.financeservice.dto.SprawozdanieDto.*;

import static rdhxb.financeservice.util.XmlUtils.findElement;
import static rdhxb.financeservice.util.XmlUtils.getChild;
import static rdhxb.financeservice.util.XmlUtils.getKwota;

// Maps the JednostkaMala RZiSPor onto the full (JednostkaInna) model.
// JednostkaMala has no "zysk z działalności operacyjnej", so from F on its letters are shifted by one:
// Mala F -> G, G -> H, H -> I, I -> J, J -> L.
@Component
public class RachunekZyskowIStratMalaParser {

    public RachunekZyskowIStrat parse(Document doc, String wariantRzis) {
        if (!"RZiSPor".equals(wariantRzis)) {
            return null; // RZiSKalk uses different letters - not supported yet
        }
        Element rzis = findElement(doc.getDocumentElement(), "RZiSPor");
        if (rzis == null) {
            return null;
        }

        return new RachunekZyskowIStrat(
                przychodyNetto(getChild(rzis, "A")),
                koszty(getChild(rzis, "B")),
                getKwota(getChild(rzis, "C")),
                pozostalePrzychody(getChild(rzis, "D")),
                pozostaleKoszty(getChild(rzis, "E")),
                null, // not reported by JednostkaMala
                przychodyFinansowe(getChild(rzis, "F")),
                kosztyFinansowe(getChild(rzis, "G")),
                getKwota(getChild(rzis, "H")),
                getKwota(getChild(rzis, "I")),
                null, // not reported by JednostkaMala
                getKwota(getChild(rzis, "J"))
        );
    }

    // A_I covers sales of products AND goods, so it fits neither zeSprzedazyProduktow nor zeSprzedazyTowarow
    private PrzychodyNetto przychodyNetto(Element e) {
        if (e == null) return null;
        return new PrzychodyNetto(getKwota(e), null, null, k(e, "II"), k(e, "III"), null);
    }

    // B_VI "pozostałe koszty" also contains taxes and goods sold, so only its "w tym" B_VI_1 is mapped
    private KosztyDzialalnosciOperacyjnej koszty(Element e) {
        if (e == null) return null;
        return new KosztyDzialalnosciOperacyjnej(
                getKwota(e),
                k(e, "I"),
                k(e, "II"),
                k(e, "III"),
                null,
                k(e, "IV"),
                wTym(c(e, "V"), "1"),
                null,
                k(c(e, "VI"), "1")
        );
    }

    private PozostalePrzychodyOperacyjne pozostalePrzychody(Element e) {
        if (e == null) return null;
        return new PozostalePrzychodyOperacyjne(getKwota(e), null, null, k(e, "1"), null);
    }

    private PozostaleKosztyOperacyjne pozostaleKoszty(Element e) {
        if (e == null) return null;
        return new PozostaleKosztyOperacyjne(getKwota(e), null, k(e, "1"), null);
    }

    private PrzychodyFinansowe przychodyFinansowe(Element e) {
        if (e == null) return null;
        Element dywidendy = c(e, "I");
        Element odPowiazanych = c(dywidendy, "1");
        return new PrzychodyFinansowe(
                getKwota(e),
                dywidendy == null ? null : new Dywidendy(
                        getKwota(dywidendy),
                        odPowiazanych == null ? null : new KwotaWTym(getKwota(odPowiazanych), null),
                        null
                ),
                wTym(c(e, "II"), "1"),
                wTym(c(e, "III"), "1"),
                k(e, "IV"),
                null
        );
    }

    private KosztyFinansowe kosztyFinansowe(Element e) {
        if (e == null) return null;
        return new KosztyFinansowe(
                getKwota(e),
                wTym(c(e, "I"), "1"),
                wTym(c(e, "II"), "1"),
                k(e, "III"),
                null
        );
    }

    // Position with its "w tym" child, e.g. wTym(F_II, "1") -> F_II + F_II_1
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
