package rdhxb.krsservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.common.krs.KrsData;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.krsservice.Client.KrsClient;
import rdhxb.krsservice.config.RabbitMqConfig;
import rdhxb.krsservice.dto.KrsApiResponse;
import rdhxb.krsservice.krsMapper.KrsMapper;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.krsservice.entity.KrsModel;
import rdhxb.krsservice.repository.KrsRepository;


import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class KrsService {

    private final KrsClient krsClient;
    private final KrsRepository repo;
    private final KrsMapper mapper;


    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @SendTo(RabbitMqConfig.REPLIES)
    public RepliesModel receiveKrsNumber(KRSNumberModel krsMess){

        if (krsMess.getKrsNum() == null){
            log.error("Krs number not found !");
            return null;
        }else {
            try {
                KrsApiResponse data = krsClient.getData(krsMess.getKrsNum());
                KrsModel krsModel = mapper.toKrsModel(data, krsMess.getId());
                KrsModel modelInDb = repo.findKrsModelByKrsNumber(krsMess.getKrsNum());
                if (krsModel != null) {
                    if (modelInDb == null){
                        saveKrsToDb(krsModel);
                        log.info("Saved KrsModel to db !");
                    } else if (!Objects.equals(krsModel.getDataOstatniegoWpisu(), modelInDb.getDataOstatniegoWpisu())) {
                        adjustData(krsModel, modelInDb);
                        log.info("Updated data fetch !");
                    }else {
                        modelInDb.setFetchedAt(krsModel.getFetchedAt());
                        saveKrsToDb(modelInDb);
                        log.info("No new data fetch updated !");
                    }


                    KrsData krsData = mapper.toKrsData(krsModel);
                    RepliesModel repliesModel = new RepliesModel();
                    repliesModel.setRequestId(krsMess.getId());
                    repliesModel.setSource(Source.KRS);
                    repliesModel.setPayload(krsData);
                    repliesModel.setStatus(Status.SUCCESS);
                    return repliesModel;

                }

            } catch (Exception e) {
                RepliesModel repliesFailed = new RepliesModel();
                repliesFailed.setRequestId(krsMess.getId());
                repliesFailed.setStatus(Status.FAILED);
                repliesFailed.setPayload(null);
                repliesFailed.setSource(Source.KRS);
                repliesFailed.setErrorMessage(e.getMessage());
                log.error("Failed ! {}", e.getMessage(), e);
                return repliesFailed;
            }
        }

        return null;
    }

    public void saveKrsToDb(KrsModel krsModel){
        repo.save(krsModel);
    }

    public void adjustData(KrsModel krsModel, KrsModel modelInDb){
        krsModel.setId(modelInDb.getId());
        repo.save(krsModel);
    }

}
