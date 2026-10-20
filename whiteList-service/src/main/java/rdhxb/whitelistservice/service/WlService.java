package rdhxb.whitelistservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.base.Status;
import rdhxb.common.nip.NIPMessage;
import rdhxb.common.nip.WlData;
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
    @SendTo(RabbitMqConfig.REPLIES)
    public RepliesModel receiveNipNumber(NIPMessage nipMessage){

        RepliesModel replies = new RepliesModel();

        if (nipMessage.getNIP() == null){
            log.error("NIP number not found !!");
            return null;
        }

            try {
                WlApiResponse dataResponse = wlClient.getData(nipMessage.getNIP());
                WlModel model = wlMapper.toWlModel(dataResponse,nipMessage.getRequestid());
                WlModel dataInDb = wlRepo.getWlModelByNip(nipMessage.getNIP());

                if (model != null) {


                    if (!wlRepo.existsByNip(nipMessage.getNIP())) {
                        wlRepo.save(model);
                        log.info("New data fetch saving to db !");
                    } else if (wlRepo.existsByNip(nipMessage.getNIP())) {
                        refreshWlData(dataInDb, model);
                        log.info("Is in db refreshing data if not null ");
                    }

                    replies.setSource(Source.WL);
                    replies.setRequestId(nipMessage.getRequestid());
                    replies.setStatus(Status.SUCCESS);
                    replies.setPayload(wlMapper.toWlData(model));
                    return replies;

                }
            }catch (Exception e){
                log.error(e.getMessage());
                replies.setStatus(Status.FAILED);
                replies.setRequestId(nipMessage.getRequestid());
                replies.setSource(Source.WL);
                replies.setErrorMessage(e.getMessage());
                replies.setPayload(null); // mby should send old data from db ? and just inform that was a problem and data could be not correct ?
                return replies;
            }

            return null;


    }

    public void refreshWlData(WlModel oldData, WlModel freshData){

        try {
            wlRepo.delete(oldData);
            wlRepo.save(freshData);
        }catch (Exception e){
            log.error(e.getMessage());
        }


    }
}
