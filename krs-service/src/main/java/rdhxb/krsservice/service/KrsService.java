package rdhxb.krsservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.common.model.KrsData;
import rdhxb.common.model.RepliesModel;
import rdhxb.common.model.Source;
import rdhxb.common.model.Status;
import rdhxb.krsservice.Client.KrsClient;
import rdhxb.krsservice.config.RabbitMqConfig;
import rdhxb.krsservice.dto.KrsApiResponse;
import rdhxb.krsservice.krsMapper.KrsMapper;
import rdhxb.common.model.KRSNumberModel;
import rdhxb.krsservice.model.KrsModel;
import rdhxb.krsservice.repository.KrsRepository;


import java.util.UUID;

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
            return null;
        }else {
            try {
                KrsApiResponse data = krsClient.getData(krsMess.getKrsNum());
                KrsModel krsModel = mapper.toKrsModel(data, UUID.fromString(krsMess.getId()));
                if (krsModel != null) {
                    saveKrsToDb(krsModel);

                    KrsData krsData = mapper.toKrsData(krsModel);
                    RepliesModel repliesModel = new RepliesModel();
                    repliesModel.setRequestId(UUID.fromString(krsMess.getId()));
                    repliesModel.setSource(Source.KRS);
                    repliesModel.setPayload(krsData);
                    repliesModel.setStatus(Status.SUCCESS);
                    return repliesModel;

                }

            } catch (Exception e) {
                RepliesModel repliesFailed = new RepliesModel();
                repliesFailed.setRequestId(UUID.fromString(krsMess.getId()));
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

}
