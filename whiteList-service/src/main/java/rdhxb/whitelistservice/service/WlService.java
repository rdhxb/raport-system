package rdhxb.whitelistservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.common.nip.NIPMessage;
import rdhxb.whitelistservice.client.WlClient;
import rdhxb.whitelistservice.config.RabbitMqConfig;
import rdhxb.whitelistservice.entity.WlModel;
import rdhxb.whitelistservice.model.WlApiResponse;
import rdhxb.whitelistservice.repository.WlRepository;
import rdhxb.whitelistservice.whiteListMapper.WlMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class WlService {

    private final WlClient wlClient;
    private final WlMapper wlMapper;
    private final WlRepository wlRepo;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void receiveNipNumber(NIPMessage nipMessage){

        log.info("Got nip message from exchanger in wl service !!");

        if (nipMessage.getNIP() != null){
            try {
                WlApiResponse dataResponse = wlClient.getData(nipMessage.getNIP());
                WlModel wlModel = wlMapper.toWlModel(dataResponse,nipMessage.getRequestid());
                wlRepo.save(wlModel);
                log.info("Wl Model successfully saved in db !");

            }catch (Exception e){
                log.error(e.getMessage());
            }
        }




    }
}
