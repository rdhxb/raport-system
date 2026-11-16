package rdhxb.krsservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.common.exception.PermanentApiException;
import rdhxb.common.exception.TransientApiException;
import rdhxb.common.krs.KrsData;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.krsservice.client.KrsClient;
import rdhxb.krsservice.config.RabbitMqConfig;
import rdhxb.krsservice.dto.KrsApiResponse;
import rdhxb.krsservice.krsMapper.KrsMapper;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.krsservice.entity.KrsModel;
import rdhxb.krsservice.repository.KrsRepository;


import java.io.IOException;
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

        RepliesModel replies = new RepliesModel();
        replies.setRequestId(krsMess.getId());
        replies.setSource(Source.KRS);

        if (krsMess.getKrsNum() == null){
            replies.setStatus(Status.FAILED);
            replies.setErrorMessage("Krs number not found !");
            return replies;
        }else {
            try {
                KrsApiResponse data;
                try {
                    data = krsClient.getData(krsMess.getKrsNum());
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    throw new TransientApiException("Can't get data from API KRS : " + e.getMessage(), e);
                }catch (IOException e){
                    throw new TransientApiException("Can't get data from API KRS : " + e.getMessage(), e);
                }
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
                    replies.setPayload(krsData);
                    replies.setStatus(Status.SUCCESS);
                    return replies;
                }
            } catch (PermanentApiException e) {
                replies.setStatus(Status.FAILED);
                replies.setPayload(null);
                replies.setErrorMessage(e.getMessage());
                log.error("Failed ! {}", e.getMessage(), e);
                return replies;
            }
        }
        replies.setStatus(Status.FAILED);
        replies.setErrorMessage("NO KRS API for krsNum : " + krsMess.getKrsNum() + " requestId : " + krsMess.getId());
        return replies;
    }

    public void saveKrsToDb(KrsModel krsModel){
        repo.save(krsModel);
    }

    public void adjustData(KrsModel krsModel, KrsModel modelInDb){
        krsModel.setId(modelInDb.getId());
        repo.save(krsModel);
    }

}
