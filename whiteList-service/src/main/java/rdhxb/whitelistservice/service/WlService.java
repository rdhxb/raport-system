package rdhxb.whitelistservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.common.nip.NIPMessage;
import rdhxb.whitelistservice.config.RabbitMqConfig;

@Slf4j
@Service
public class WlService {

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void receiveNipNumber(NIPMessage nipMessage){
        System.out.println(nipMessage.getNIP());
        System.out.println(nipMessage.getRequestid());
        log.info("Got nip message from exchanger in wl service !!");
    }
}
