package rdhxb.financeservice.parser.jednostkamala;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rdhxb.financeservice.dto.SprawozdanieDto.*;

import static rdhxb.financeservice.util.XmlUtils.findElement;
import static rdhxb.financeservice.util.XmlUtils.getChild;
import static rdhxb.financeservice.util.XmlUtils.getKwota;

// Maps the simplified JednostkaMala balance sheet onto the full (JednostkaInna) model.
// A sub-position is filled only when its meaning is identical in both schemas, otherwise it stays null.
@Component
public class BilansMalaParser {

    public Bilans parse(Document doc) {
        Element bilans = findElement(doc.getDocumentElement(), "BilansJednostkaMala");
        if (bilans == null) {
            return null;
        }
        return new Bilans(
                aktywa(getChild(bilans, "Aktywa")),
                pasywa(getChild(bilans, "Pasywa"))
        );
    }

    // ===================== Aktywa =====================

    private Aktywa aktywa(Element e) {
        if (e == null) return null;
        return new Aktywa(
                getKwota(e),
                aktywaTrwale(c(e, "A")),
                aktywaObrotowe(c(e, "B")),
                k(e, "C"),
                k(e, "D")
        );
    }

    private AktywaTrwale aktywaTrwale(Element e) {
        if (e == null) return null;
        Element wnip = c(e, "I");
        Element rzeczowe = c(e, "II");
        Element naleznosci = c(e, "III");
        Element inwestycje = c(e, "IV");
        Element rozliczenia = c(e, "V");
        return new AktywaTrwale(
                getKwota(e),
                wnip == null ? null : new WartosciNiematerialneIPrawne(getKwota(wnip), null, null, null, null),
                rzeczowe == null ? null : new RzeczoweAktywaTrwale(
                        getKwota(rzeczowe),
                        srodkiTrwale(c(rzeczowe, "1")),
                        k(rzeczowe, "2"),
                        null
                ),
                naleznosci == null ? null : new NaleznosciDlugoterminowe(getKwota(naleznosci), null, null, null),
                inwestycje == null ? null : new InwestycjeDlugoterminowe(
                        getKwota(inwestycje),
                        k(inwestycje, "1"),
                        null,
                        dlugoterminoweAktywaFinansowe(c(inwestycje, "2")),
                        null
                ),
                rozliczenia == null ? null : new DlugoterminoweRozliczeniaMiedzyokresowe(getKwota(rozliczenia), null, null)
        );
    }

    private SrodkiTrwale srodkiTrwale(Element e) {
        if (e == null) return null;
        return new SrodkiTrwale(getKwota(e), null, null, null, null, null);
    }

    private DlugoterminoweAktywaFinansowe dlugoterminoweAktywaFinansowe(Element e) {
        if (e == null) return null;
        return new DlugoterminoweAktywaFinansowe(getKwota(e), null, null, null);
    }

    private AktywaObrotowe aktywaObrotowe(Element e) {
        if (e == null) return null;
        Element zapasy = c(e, "I");
        Element naleznosci = c(e, "II");
        return new AktywaObrotowe(
                getKwota(e),
                zapasy == null ? null : new Zapasy(getKwota(zapasy), null, null, null, null, null),
                // Aktywa_B_II_A (trade receivables) is not split by counterparty, so it has no field here
                naleznosci == null ? null : new NaleznosciKrotkoterminowe(getKwota(naleznosci), null, null, null),
                inwestycjeKrotkoterminowe(c(e, "III")),
                k(e, "IV")
        );
    }

    private InwestycjeKrotkoterminowe inwestycjeKrotkoterminowe(Element e) {
        if (e == null) return null;
        Element aktywaFinansowe = c(e, "A");
        Element srodkiPieniezne = c(aktywaFinansowe, "1");
        return new InwestycjeKrotkoterminowe(
                getKwota(e),
                aktywaFinansowe == null ? null : new KrotkoterminoweAktywaFinansowe(
                        getKwota(aktywaFinansowe),
                        null,
                        null,
                        srodkiPieniezne == null ? null : new SrodkiPieniezne(null, getKwota(srodkiPieniezne), null, null)
                ),
                null
        );
    }

    // ===================== Pasywa =====================

    private Pasywa pasywa(Element e) {
        if (e == null) return null;
        return new Pasywa(getKwota(e), kapitalWlasny(c(e, "A")), zobowiazaniaIRezerwy(c(e, "B")));
    }

    private KapitalWlasny kapitalWlasny(Element e) {
        if (e == null) return null;
        Element zapasowy = c(e, "II");
        Element zAktualizacji = c(e, "III");
        Element rezerwowe = c(e, "IV");
        return new KapitalWlasny(
                getKwota(e),
                k(e, "I"),
                zapasowy == null ? null : new KapitalZapasowy(getKwota(zapasowy), k(zapasowy, "1")),
                zAktualizacji == null ? null : new KapitalZAktualizacjiWyceny(getKwota(zAktualizacji), k(zAktualizacji, "1")),
                rezerwowe == null ? null : new PozostaleKapitalyRezerwowe(getKwota(rezerwowe), null, null),
                k(e, "V"),
                k(e, "VI"),
                k(e, "VII")
        );
    }

    private ZobowiazaniaIRezerwy zobowiazaniaIRezerwy(Element e) {
        if (e == null) return null;
        Element rezerwy = c(e, "I");
        Element emerytalne = c(rezerwy, "1");
        Element dlugoterminowe = c(e, "II");
        Element krotkoterminowe = c(e, "III");
        Element rozliczenia = c(e, "IV");
        return new ZobowiazaniaIRezerwy(
                getKwota(e),
                rezerwy == null ? null : new Rezerwy(
                        getKwota(rezerwy),
                        null,
                        emerytalne == null ? null : new DlugoIKrotkoterminowe(getKwota(emerytalne), null, null),
                        null
                ),
                // Pasywa_B_II_1 (loans) is not split by counterparty, so it has no field here
                dlugoterminowe == null ? null : new ZobowiazaniaDlugoterminowe(getKwota(dlugoterminowe), null, null, null),
                // Pasywa_B_III_A/B (loans, trade payables) are not split by counterparty either
                krotkoterminowe == null ? null : new ZobowiazaniaKrotkoterminowe(
                        getKwota(krotkoterminowe),
                        null,
                        null,
                        null,
                        k(krotkoterminowe, "C")
                ),
                rozliczenia == null ? null : new BierneRozliczeniaMiedzyokresowe(getKwota(rozliczenia), null, null)
        );
    }

    // Child symbol = parent symbol + "_" + suffix, e.g. c(Aktywa_B, "II") -> Aktywa_B_II
    private static Element c(Element parent, String suffix) {
        return parent == null ? null : getChild(parent, parent.getLocalName() + "_" + suffix);
    }

    private static Kwota k(Element parent, String suffix) {
        return getKwota(c(parent, suffix));
    }
}
