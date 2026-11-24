package rdhxb.financeservice.parser.jednostkamala;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import rdhxb.financeservice.dto.SprawozdanieDto.Bilans;
import rdhxb.financeservice.dto.SprawozdanieDto.Metadane;
import rdhxb.financeservice.dto.SprawozdanieDto.RachunekZyskowIStrat;
import rdhxb.financeservice.dto.SprawozdanieDto.Sprawozdanie;

@Component
@RequiredArgsConstructor
public class JednostkaMalaParser {

    private final MetadaneMalaParser metadaneParser;
    private final BilansMalaParser bilansParser;
    private final RachunekZyskowIStratMalaParser rachunekZyskowIStratParser;

    public Sprawozdanie parse(Document doc) {
        Metadane metadata = metadaneParser.parse(doc);
        Bilans bilans = bilansParser.parse(doc);
        RachunekZyskowIStrat rzis = rachunekZyskowIStratParser.parse(doc, metadata.wariantRzis());

        return new Sprawozdanie(metadata, bilans, rzis);
    }
}
