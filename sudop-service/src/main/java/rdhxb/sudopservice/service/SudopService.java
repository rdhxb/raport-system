package rdhxb.sudopservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.common.base.RepliesModel;
import rdhxb.common.base.Source;
import rdhxb.common.nip.NIPMessage;
import rdhxb.sudopservice.client.SudopClient;
import rdhxb.sudopservice.config.RabbitMqConfig;


@Service
@RequiredArgsConstructor
@Slf4j
public class SudopService {

    private final SudopClient sudopClient;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @SendTo(RabbitMqConfig.REPLIES)
    public RepliesModel receiveNipNumber(NIPMessage nipMessage){
        RepliesModel repliesModel = new RepliesModel();
        repliesModel.setSource(Source.SUDOP);
        repliesModel.setPayload("FROM SUDOP SERVICE");
        log.info("PROCESSING !");


        if (nipMessage.getNIP() == null){
            return repliesModel;
        }else{
            try {
                var data = sudopClient.getData(nipMessage.getNIP());
                System.out.println(data.liczbaWynikow());
                if (data.liczbaWynikow() > 0){
                    System.out.println(data.wyniki().getFirst());
                    // process data

                }

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return repliesModel;


    }
}
