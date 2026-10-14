package rdhxb.raportservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rdhxb.raportservice.config.RabbitMqConfig;
import rdhxb.raportservice.model.KRSNumberModel;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RaportService {

    private final RabbitTemplate rabbitTemplate;

    public void sendKrsToExchanger(String krs){
        KRSNumberModel krsModel = new KRSNumberModel();
        krsModel.setId(UUID.randomUUID().toString());
        krsModel.setKrsNum(krs);
        rabbitTemplate.convertAndSend(RabbitMqConfig.REQUESTS_EXCHANGE,"",krsModel);
    }

    @RabbitListener(queues = RabbitMqConfig.REPLIES)
    public void getReplies(String replies){
        System.out.println(replies);
    }

}
