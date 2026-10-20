package rdhxb.whitelistservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.common.nip.NIPMessage;
import rdhxb.whitelistservice.client.WlClient;
import rdhxb.whitelistservice.config.RabbitMqConfig;
import rdhxb.whitelistservice.model.WlApiResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class WlService {

    private final WlClient wlClient;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void receiveNipNumber(NIPMessage nipMessage){
        System.out.println(nipMessage.getNIP());
        System.out.println(nipMessage.getRequestid());
        log.info("Got nip message from exchanger in wl service !!");

        try {
            WlApiResponse dataResponse = wlClient.getData(nipMessage.getNIP());
            System.out.println(dataResponse);
            log.info("Data collected from API wl !!");
        }catch (Exception e){
            log.error(e.getMessage());
        }



    }
}
