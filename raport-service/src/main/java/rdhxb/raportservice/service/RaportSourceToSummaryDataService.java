package rdhxb.raportservice.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rdhxb.common.base.Source;

import rdhxb.common.crbr.CrbrData;
import rdhxb.common.krs.KrsData;
import rdhxb.common.nip.WlData;
import rdhxb.common.ceidg.CeidgData;
import rdhxb.common.sudop.SudopData;
import rdhxb.common.summary.SummaryData;
import rdhxb.raportservice.dto.RaportDto;
import rdhxb.raportservice.dto.RaportSourceDto;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RaportSourceToSummaryDataService {

    public SummaryData processRaport(RaportDto raportDto, ObjectMapper mapper){
        List<RaportSourceDto> raportSources = raportDto.raportSources();
        UUID requestId = raportDto.requestId();
        SummaryData summaryData = new SummaryData();
        summaryData.setRequestId(requestId);

        for (RaportSourceDto raportSource : raportSources){
            Source source = raportSource.source();
            switch (source){
                case KRS -> {
                    KrsData krsData = mapper.readValue(raportSource.payload(), KrsData.class);
                    summaryData.setKrs(krsData);
                }
                case WL -> {
                    WlData wlData = mapper.readValue(raportSource.payload(), WlData.class);
                    summaryData.setWl(wlData);
                }
                case CEIDG -> {
                    CeidgData ceidgData = mapper.readValue(raportSource.payload(), CeidgData.class);
                    summaryData.setCeidg(ceidgData);
                }
                case CRBR -> {
                    CrbrData crbrData = mapper.readValue(raportSource.payload(), CrbrData.class);
                    summaryData.setCrbr(crbrData);
                }
                case SUDOP -> {
                    SudopData sudopData = mapper.readValue(raportSource.payload(), SudopData.class);
                    summaryData.setSudop(sudopData);
                }
                case null, default -> {
                    log.info("No case for source : " + source);
                }
            }

        }
        return summaryData;

    }

}
