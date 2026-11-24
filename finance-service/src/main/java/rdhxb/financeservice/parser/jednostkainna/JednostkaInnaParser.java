package rdhxb.financeservice.parser.jednostkainna;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import rdhxb.financeservice.dto.SprawozdanieDto.Bilans;
import rdhxb.financeservice.dto.SprawozdanieDto.Metadane;
import rdhxb.financeservice.dto.SprawozdanieDto.RachunekZyskowIStrat;
import rdhxb.financeservice.dto.SprawozdanieDto.Sprawozdanie;

@Component
@RequiredArgsConstructor
public class JednostkaInnaParser {

    private final MetadaneParser metadaneParser;
    private final BilansParser bilansParser;
    private final RachunekZyskowIStratParser rachunekZyskowIStratParser;

    public Sprawozdanie parse(Document doc) {
        Metadane metadata = metadaneParser.parse(doc);
        Bilans bilans = bilansParser.parse(doc);
        RachunekZyskowIStrat rzis = rachunekZyskowIStratParser.parse(doc, metadata.wariantRzis());

        return new Sprawozdanie(metadata, bilans, rzis);
    }
}
