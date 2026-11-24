package rdhxb.financeservice.parser.jednostkamala;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rdhxb.financeservice.dto.SprawozdanieDto.Bilans;
import rdhxb.financeservice.dto.SprawozdanieDto.Metadane;
import rdhxb.financeservice.dto.SprawozdanieDto.RachunekZyskowIStrat;
import rdhxb.financeservice.dto.SprawozdanieDto.Sprawozdanie;
import rdhxb.financeservice.parser.jednostkainna.BilansParser;
import rdhxb.financeservice.parser.jednostkainna.RachunekZyskowIStratParser;

import static rdhxb.financeservice.util.XmlUtils.findElement;

@Component
@RequiredArgsConstructor
@Slf4j
public class JednostkaMalaParser {

    private final MetadaneMalaParser metadaneParser;
    private final BilansMalaParser bilansParser;
    private final BilansParser bilansInnaParser;
    private final RachunekZyskowIStratMalaParser rachunekZyskowIStratParser;
    private final RachunekZyskowIStratParser rachunekZyskowIStratParserInna;

    public Sprawozdanie parse(Document doc) {
        Metadane metadata = metadaneParser.parse(doc);
        Element bilansMala = findElement(doc.getDocumentElement(), "BilansJednostkaMala");
        Element bilansInna = findElement(doc.getDocumentElement(), "BilansJednostkaInna");

        Element rzisMalaRoot = findElement(doc.getDocumentElement(), "RZiSJednostkaMala");
        Element rzisMala = findElement(rzisMalaRoot, "RZiSPor");
        Element rzisInnaRoot = findElement(doc.getDocumentElement(), "RZiSJednostkaInna");
        Element rzisInna = findElement(rzisInnaRoot, "RZiSPor");
        RachunekZyskowIStrat rzis;

        Bilans bilans;
        if (bilansMala != null){
            bilans = bilansParser.parse(bilansMala);
            log.info("Bilans z jednostka mala !");
        }else{
            bilans = bilansInnaParser.parse(bilansInna);
            log.info("Bilans z jednostka Inna !");
        }
        if (rzisMala != null){
            rzis = rachunekZyskowIStratParser.parse(rzisMala, metadata.wariantRzis());
            log.info("Rzis mala");
        }else {
            rzis = rachunekZyskowIStratParserInna.parse(rzisInna, metadata.wariantRzis());
            log.info("rzis inna");
        }

        return new Sprawozdanie(metadata, bilans, rzis);
    }
}
