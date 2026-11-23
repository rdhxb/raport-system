package rdhxb.financeservice.parser;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rdhxb.financeservice.dto.SprawozdanieDto.*;

import static rdhxb.financeservice.util.XmlUtils.findElement;
import static rdhxb.financeservice.util.XmlUtils.getChild;
import static rdhxb.financeservice.util.XmlUtils.getKwota;

// Every group method returns null when its element is missing in the XML
@Component
public class BilansParser {

    public Bilans parse(Document doc) {
        Element bilans = findElement(doc.getDocumentElement(), "Bilans");
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
        return new AktywaTrwale(
                getKwota(e),
                wartosciNiematerialne(c(e, "I")),
                rzeczoweAktywaTrwale(c(e, "II")),
                naleznosciDlugoterminowe(c(e, "III")),
                inwestycjeDlugoterminowe(c(e, "IV")),
                dlugoterminoweRozliczenia(c(e, "V"))
        );
    }

    private WartosciNiematerialneIPrawne wartosciNiematerialne(Element e) {
        if (e == null) return null;
        return new WartosciNiematerialneIPrawne(getKwota(e), k(e, "1"), k(e, "2"), k(e, "3"), k(e, "4"));
    }

    private RzeczoweAktywaTrwale rzeczoweAktywaTrwale(Element e) {
        if (e == null) return null;
        return new RzeczoweAktywaTrwale(getKwota(e), srodkiTrwale(c(e, "1")), k(e, "2"), k(e, "3"));
    }

    private SrodkiTrwale srodkiTrwale(Element e) {
        if (e == null) return null;
        return new SrodkiTrwale(getKwota(e), k(e, "A"), k(e, "B"), k(e, "C"), k(e, "D"), k(e, "E"));
    }

    private NaleznosciDlugoterminowe naleznosciDlugoterminowe(Element e) {
        if (e == null) return null;
        return new NaleznosciDlugoterminowe(getKwota(e), k(e, "1"), k(e, "2"), k(e, "3"));
    }

    private InwestycjeDlugoterminowe inwestycjeDlugoterminowe(Element e) {
        if (e == null) return null;
        return new InwestycjeDlugoterminowe(
                getKwota(e),
                k(e, "1"),
                k(e, "2"),
                dlugoterminoweAktywaFinansowe(c(e, "3")),
                k(e, "4")
        );
    }

    private DlugoterminoweAktywaFinansowe dlugoterminoweAktywaFinansowe(Element e) {
        if (e == null) return null;
        return new DlugoterminoweAktywaFinansowe(
                getKwota(e),
                aktywaFinansowe(c(e, "A")),
                aktywaFinansowe(c(e, "B")),
                aktywaFinansowe(c(e, "C"))
        );
    }

    private DlugoterminoweRozliczeniaMiedzyokresowe dlugoterminoweRozliczenia(Element e) {
        if (e == null) return null;
        return new DlugoterminoweRozliczeniaMiedzyokresowe(getKwota(e), k(e, "1"), k(e, "2"));
    }

    private AktywaObrotowe aktywaObrotowe(Element e) {
        if (e == null) return null;
        return new AktywaObrotowe(
                getKwota(e),
                zapasy(c(e, "I")),
                naleznosciKrotkoterminowe(c(e, "II")),
                inwestycjeKrotkoterminowe(c(e, "III")),
                k(e, "IV")
        );
    }

    private Zapasy zapasy(Element e) {
        if (e == null) return null;
        return new Zapasy(getKwota(e), k(e, "1"), k(e, "2"), k(e, "3"), k(e, "4"), k(e, "5"));
    }

    private NaleznosciKrotkoterminowe naleznosciKrotkoterminowe(Element e) {
        if (e == null) return null;
        return new NaleznosciKrotkoterminowe(
                getKwota(e),
                rozrachunki(c(e, "1")),
                rozrachunki(c(e, "2")),
                naleznosciOdPozostalych(c(e, "3"))
        );
    }

    private NaleznosciOdPozostalychJednostek naleznosciOdPozostalych(Element e) {
        if (e == null) return null;
        return new NaleznosciOdPozostalychJednostek(
                getKwota(e),
                wgTerminu(c(e, "A")),
                k(e, "B"),
                k(e, "C"),
                k(e, "D")
        );
    }

    private InwestycjeKrotkoterminowe inwestycjeKrotkoterminowe(Element e) {
        if (e == null) return null;
        return new InwestycjeKrotkoterminowe(getKwota(e), krotkoterminoweAktywaFinansowe(c(e, "1")), k(e, "2"));
    }

    private KrotkoterminoweAktywaFinansowe krotkoterminoweAktywaFinansowe(Element e) {
        if (e == null) return null;
        return new KrotkoterminoweAktywaFinansowe(
                getKwota(e),
                aktywaFinansowe(c(e, "A")),
                aktywaFinansowe(c(e, "B")),
                srodkiPieniezne(c(e, "C"))
        );
    }

    private SrodkiPieniezne srodkiPieniezne(Element e) {
        if (e == null) return null;
        return new SrodkiPieniezne(getKwota(e), k(e, "1"), k(e, "2"), k(e, "3"));
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
                rezerwowe == null ? null : new PozostaleKapitalyRezerwowe(getKwota(rezerwowe), k(rezerwowe, "1"), k(rezerwowe, "2")),
                k(e, "V"),
                k(e, "VI"),
                k(e, "VII")
        );
    }

    private ZobowiazaniaIRezerwy zobowiazaniaIRezerwy(Element e) {
        if (e == null) return null;
        return new ZobowiazaniaIRezerwy(
                getKwota(e),
                rezerwy(c(e, "I")),
                zobowiazaniaDlugoterminowe(c(e, "II")),
                zobowiazaniaKrotkoterminowe(c(e, "III")),
                bierneRozliczenia(c(e, "IV"))
        );
    }

    private Rezerwy rezerwy(Element e) {
        if (e == null) return null;
        return new Rezerwy(getKwota(e), k(e, "1"), dlugoIKrotko(c(e, "2")), dlugoIKrotko(c(e, "3")));
    }

    private ZobowiazaniaDlugoterminowe zobowiazaniaDlugoterminowe(Element e) {
        if (e == null) return null;
        Element pozostale = c(e, "3");
        return new ZobowiazaniaDlugoterminowe(
                getKwota(e),
                k(e, "1"),
                k(e, "2"),
                pozostale == null ? null : new ZobowiazaniaDlugoterminoweWobecPozostalych(
                        getKwota(pozostale),
                        k(pozostale, "A"),
                        k(pozostale, "B"),
                        k(pozostale, "C"),
                        k(pozostale, "D"),
                        k(pozostale, "E")
                )
        );
    }

    private ZobowiazaniaKrotkoterminowe zobowiazaniaKrotkoterminowe(Element e) {
        if (e == null) return null;
        return new ZobowiazaniaKrotkoterminowe(
                getKwota(e),
                rozrachunki(c(e, "1")),
                rozrachunki(c(e, "2")),
                zobowiazaniaKrotkoterminoweWobecPozostalych(c(e, "3")),
                k(e, "4")
        );
    }

    private ZobowiazaniaKrotkoterminoweWobecPozostalych zobowiazaniaKrotkoterminoweWobecPozostalych(Element e) {
        if (e == null) return null;
        return new ZobowiazaniaKrotkoterminoweWobecPozostalych(
                getKwota(e),
                k(e, "A"),
                k(e, "B"),
                k(e, "C"),
                wgTerminu(c(e, "D")),
                k(e, "E"),
                k(e, "F"),
                k(e, "G"),
                k(e, "H"),
                k(e, "I")
        );
    }

    private BierneRozliczeniaMiedzyokresowe bierneRozliczenia(Element e) {
        if (e == null) return null;
        return new BierneRozliczeniaMiedzyokresowe(getKwota(e), k(e, "1"), dlugoIKrotko(c(e, "2")));
    }

    // ===================== Shared shapes =====================

    private AktywaFinansowe aktywaFinansowe(Element e) {
        if (e == null) return null;
        return new AktywaFinansowe(getKwota(e), k(e, "1"), k(e, "2"), k(e, "3"), k(e, "4"));
    }

    private RozrachunkiZJednostka rozrachunki(Element e) {
        if (e == null) return null;
        return new RozrachunkiZJednostka(getKwota(e), wgTerminu(c(e, "A")), k(e, "B"));
    }

    private WgTerminu wgTerminu(Element e) {
        if (e == null) return null;
        return new WgTerminu(getKwota(e), k(e, "1"), k(e, "2"));
    }

    private DlugoIKrotkoterminowe dlugoIKrotko(Element e) {
        if (e == null) return null;
        return new DlugoIKrotkoterminowe(getKwota(e), k(e, "1"), k(e, "2"));
    }

    // Child symbol = parent symbol + "_" + suffix, e.g. c(Aktywa_B, "II") -> Aktywa_B_II
    private static Element c(Element parent, String suffix) {
        return parent == null ? null : getChild(parent, parent.getLocalName() + "_" + suffix);
    }

    private static Kwota k(Element parent, String suffix) {
        return getKwota(c(parent, suffix));
    }
}
