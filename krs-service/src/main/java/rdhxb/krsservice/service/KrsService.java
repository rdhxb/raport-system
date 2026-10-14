package rdhxb.krsservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.krsservice.Client.KrsClient;
import rdhxb.krsservice.config.RabbitMqConfig;
import rdhxb.krsservice.dto.KrsApiResponse;
import rdhxb.krsservice.model.KRSNumberModel;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class KrsService {

    private final KrsClient krsClient;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
//    @SendTo(RabbitMqConfig.REPLIES)
    public void receiveKrsNumber(KRSNumberModel krsMess){



        if (krsMess.getKrsNum() == null){
            return;
        }
        try {
            KrsApiResponse data = krsClient.getData(krsMess.getKrsNum());
            System.out.println("Data collected from API !!");
            System.out.println(data);
        }catch (Exception e){
            throw  new AmqpRejectAndDontRequeueException("Krs fetched failed for : " + krsMess.getKrsNum() , e);
        }


//        return krsNum.getKrsNum();

    }

}
