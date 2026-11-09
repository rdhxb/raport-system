package rdhxb.sudopservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.common.nip.NIPMessage;
import rdhxb.sudopservice.client.SudopClient;
import rdhxb.sudopservice.config.RabbitMqConfig;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class SudopService {

    private final SudopClient sudopClient;

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public void receiveNipNumber(NIPMessage nipMessage){

        if (nipMessage.getNIP() == null){
            return;
        }else{
            try {
                sudopClient.getData(nipMessage.getNIP());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
