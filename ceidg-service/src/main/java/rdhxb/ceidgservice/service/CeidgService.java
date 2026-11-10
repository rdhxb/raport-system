package rdhxb.ceidgservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.common.nip.NIPMessage;
import rdhxb.ceidgservice.client.CeidgClient;
import rdhxb.ceidgservice.config.RabbitMqConfig;

@Service
@RequiredArgsConstructor
@Slf4j
public class CeidgService {

    private final CeidgClient ceidgClient;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void receiveNipNumber(NIPMessage nipMessage){

        if (nipMessage.getNIP() == null){
            return;
        }

        try {
            var data = ceidgClient.getData(nipMessage.getNIP());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
