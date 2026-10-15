package rdhxb.krsservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.krsservice.Client.KrsClient;
import rdhxb.krsservice.config.RabbitMqConfig;
import rdhxb.krsservice.dto.KrsApiResponse;
import rdhxb.krsservice.krsMapper.KrsMapper;
import rdhxb.krsservice.model.KRSNumberModel;
import rdhxb.krsservice.model.KrsModel;
import rdhxb.krsservice.repository.KrsRepository;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KrsService {

    private final KrsClient krsClient;
    private final KrsRepository repo;
    private final KrsMapper mapper;

    @Transactional
    @RabbitListener(queues = RabbitMqConfig.QUEUE)
//    @SendTo(RabbitMqConfig.REPLIES)
    public void receiveKrsNumber(KRSNumberModel krsMess){

        if (krsMess.getKrsNum() == null){
            return;
        }

        try {
            KrsApiResponse data = krsClient.getData(krsMess.getKrsNum());
            KrsModel krsModel = mapper.toKrsModel(data, UUID.fromString(krsMess.getId()));
            if (krsModel != null){
                saveKrsToDb(krsModel);
            }

            System.out.println("Data collected from API !!");
        }catch (Exception e){
            throw  new AmqpRejectAndDontRequeueException("Krs fetched failed for : " + krsMess.getKrsNum() , e);
        }
//        return krsNum.getKrsNum();
    }

    @Transactional
    public void saveKrsToDb(KrsModel krsModel){
        repo.save(krsModel);
    }

}
