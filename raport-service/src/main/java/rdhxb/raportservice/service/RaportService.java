package rdhxb.raportservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.LoggingEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rdhxb.common.model.*;
import rdhxb.raportservice.config.RabbitMqConfig;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaportService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;

    public void sendKrsToExchanger(String krs){
        KRSNumberModel krsModel = new KRSNumberModel();
        krsModel.setId(UUID.randomUUID().toString());
        krsModel.setKrsNum(krs);
        rabbitTemplate.convertAndSend(RabbitMqConfig.REQUESTS_EXCHANGE,"",krsModel);
    }

    @RabbitListener(queues = RabbitMqConfig.REPLIES)
    public void getReplies(RepliesModel replies){
        if (replies == null){
            return;
        }
        if (replies.getStatus() == Status.SUCCESS) {
            try {
                var data = convertPayload(replies);
                System.out.println(data);

            } catch (Exception e) {
                System.out.println("Cant convert playload for request id : " + replies.getRequestId());

            }
        } else if (replies.getStatus() == Status.FAILED) {
            log.error("FAILED !! requestId {} source : {} error : {}", replies.getRequestId(), replies.getSource(), replies.getErrorMessage());

        }

    }

    public Object convertPayload(RepliesModel repliesModel){
        Source source = repliesModel.getSource();

        switch (source){
            case KRS -> {
                return mapper.convertValue(repliesModel.getPayload(), KrsData.class);
            }
            default -> {
                return null;
            }
        }

    }

}
