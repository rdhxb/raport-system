package rdhxb.financeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.finance.FinanceData;
import rdhxb.common.finance.FinanceTable;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.financeservice.config.RabbitMqConfig;
import rdhxb.financeservice.dto.SprawozdanieDto.Sprawozdanie;
import rdhxb.financeservice.entity.SprawozdanieModel;
import rdhxb.financeservice.mapper.Mapper;
import rdhxb.financeservice.parser.DocumentParser;
import rdhxb.financeservice.repository.SprawozdanieRepository;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceService {

    private final DocumentParser parser;
    private final SprawozdanieRepository repository;
    private final Mapper mapper;


    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @SendTo(RabbitMqConfig.REPLIES)
    public RepliesModel getRequest(KRSNumberModel message){

        RepliesModel replies = new RepliesModel();
        replies.setRequestId(message.getId());
        replies.setSource(Source.FINANCE);

        if (message.getKrsNum() == null){
            replies.setErrorMessage("No krs number !");
            replies.setStatus(Status.FAILED);
            return replies;
        }

        SprawozdanieModel sprawozdanieModel = repository.findFirstByKrsOrderByOkresDoDesc(message.getKrsNum());
        List<SprawozdanieModel> sprModel = repository.findAllByKrsOrderByOkresDoDesc(message.getKrsNum());

        if (sprawozdanieModel == null){
            replies.setStatus(Status.NOT_FOUND);
            replies.setErrorMessage("Not found sprawozdanie with krs : " + message.getKrsNum());
            return replies;
        }else {
            try {
                List<FinanceData> financeDataList = new ArrayList<>();
                for (SprawozdanieModel spr : sprModel){
                    financeDataList.add(mapper.sprawozdanieModelToData(spr));
                }

                try {
                    FinanceTable ft = buildFinanceTable(financeDataList);
                    replies.setPayload(ft);
                    replies.setStatus(Status.SUCCESS);
                    return replies;
                } catch (Exception e) {
                    log.error("Fail to build finance table due to : {}", e.getMessage());
                    replies.setStatus(Status.FAILED);
                    return replies;
                }

            } catch (Exception e) {
                replies.setStatus(Status.FAILED);
                replies.setErrorMessage("mapper or db fail !");
                return replies;
            }

        }

    }

    @Transactional
    public String saveData(MultipartFile file) throws IOException, ParserConfigurationException, SAXException {

        String fileName = file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            Sprawozdanie sprawozdanie = parser.parseDocument(inputStream);
            SprawozdanieModel sModel = buildSprawozdanie(sprawozdanie, fileName);

            return handleDuplicates(sModel);
        }
    }

    private SprawozdanieModel buildSprawozdanie(Sprawozdanie sprawozdanie, String fileName) {
        SprawozdanieModel sprawozdanieModel = new SprawozdanieModel();
        sprawozdanieModel.setKrs(sprawozdanie.metadane().krs());
        sprawozdanieModel.setNip(sprawozdanie.metadane().nip());
        sprawozdanieModel.setNazwaPliku(fileName);
        sprawozdanieModel.setOkresOd(sprawozdanie.metadane().okresOd());
        sprawozdanieModel.setOkresDo(sprawozdanie.metadane().okresDo());
        sprawozdanieModel.setUploadedAt(Instant.now());
        sprawozdanieModel.setSprawozdanie(sprawozdanie);
        return sprawozdanieModel;
    }

    private FinanceTable buildFinanceTable(List<FinanceData> financeDataList){
        FinanceTable ft = mapper.toFinanceTable(financeDataList);
        log.info("FT : {}", ft);
        return ft;
    }


    private String handleDuplicates(SprawozdanieModel sModel){
        if (sModel.getKrs() == null || sModel.getOkresDo() == null){
            throw new IllegalArgumentException("Brak KRS lub okresu w sprawozdaniu");
        }
        String mess = "";

        SprawozdanieModel modelInDb = repository.findByOkresDoAndKrs(sModel.getOkresDo(),sModel.getKrs());

        if (modelInDb != null){
            sModel.setId(modelInDb.getId());
            mess = "zaktualizowno dane dla roku " + sModel.getOkresDo().getYear();
        }
        else{
            mess = "dodano dane dla roku " + sModel.getOkresDo().getYear();
        }
        repository.save(sModel);
        return mess;

    }
}
