package rdhxb.sudopservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.common.nip.NIPMessage;
import rdhxb.sudopservice.client.SudopClient;
import rdhxb.sudopservice.config.RabbitMqConfig;


@Service
@RequiredArgsConstructor
@Slf4j
public class SudopService {

    private final SudopClient sudopClient;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void receiveNipNumber(NIPMessage nipMessage){

        if (nipMessage.getNIP() == null){
            return;
        }else{
            try {
                var data = sudopClient.getData(nipMessage.getNIP());
                System.out.println(data.liczbaWynikow());
                System.out.println(data.wyniki().getFirst());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

    }
}
