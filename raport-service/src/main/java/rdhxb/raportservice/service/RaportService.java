package rdhxb.raportservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.krs.KRSNumberModel;
import rdhxb.common.krs.KrsData;
import rdhxb.common.nip.NIPMessage;
import rdhxb.raportservice.config.RabbitMqConfig;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RaportService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;

    private String Nip = "";
    private UUID requestId;

    public void sendKrsToExchanger(String krs){
        KRSNumberModel krsModel = new KRSNumberModel();
        krsModel.setId(UUID.randomUUID());
        krsModel.setKrsNum(krs);
        rabbitTemplate.convertAndSend(RabbitMqConfig.REQUESTS_EXCHANGE,"",krsModel);
    }

    public void sendNipToExchanger(String NIP){
        NIPMessage nipMessage = new NIPMessage();
        nipMessage.setRequestid(requestId);
        nipMessage.setNIP(NIP);
        rabbitTemplate.convertAndSend(RabbitMqConfig.NIP_EXCHANGER,"",nipMessage);
    }

    @RabbitListener(queues = RabbitMqConfig.REPLIES)
    public void getReplies(RepliesModel replies){
        if (replies == null){
            return;
        }
        if (replies.getStatus() == Status.SUCCESS) {
            try {
                KrsData data = (KrsData) convertPayload(replies);
                System.out.println(data);
                Nip = data.nip();
                requestId = replies.getRequestId();
                if (replies.getSource() == Source.KRS){
                    sendNipToExchanger(Nip);
                    log.info("NIP number sanded to exchanger from raport service !!!!");
                    log.info("Request id should be : " + replies.getRequestId());
                }


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
