package rdhxb.financeservice.parser.jednostkainna;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import rdhxb.financeservice.dto.SprawozdanieDto.Bilans;
import rdhxb.financeservice.dto.SprawozdanieDto.Metadane;
import rdhxb.financeservice.dto.SprawozdanieDto.RachunekZyskowIStrat;
import rdhxb.financeservice.dto.SprawozdanieDto.Sprawozdanie;

import static rdhxb.financeservice.util.XmlUtils.findElement;

@Component
@RequiredArgsConstructor
public class JednostkaInnaParser {

    private final MetadaneParser metadaneParser;
    private final BilansParser bilansParser;
    private final RachunekZyskowIStratParser rachunekZyskowIStratParser;

    public Sprawozdanie parse(Document doc) {
        Metadane metadata = metadaneParser.parse(doc);
        Bilans bilans;
        Element bilansInna = findElement(doc.getDocumentElement(), "Bilans");
        Element bilansInnaInna = findElement(doc.getDocumentElement(), "BilansJednostkaInna");

        Element rzisEle = findElement(doc.getDocumentElement(), "RZiSPor");

        if (bilansInna != null){
            bilans = bilansParser.parse(bilansInna);
        }else {
            bilans = bilansParser.parse(bilansInnaInna);
        }


        RachunekZyskowIStrat rzis = rachunekZyskowIStratParser.parse(rzisEle, metadata.wariantRzis());

        return new Sprawozdanie(metadata, bilans, rzis);
    }
}
