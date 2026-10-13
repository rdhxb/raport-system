package rdhxb.krsservice.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import rdhxb.krsservice.config.RabbitMqConfig;
import rdhxb.krsservice.model.TestModel;

@Service
public class ConsumerTestService {

//    process and replay OK
    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    @SendTo(RabbitMqConfig.REPLIES)
    public String processTestMess(TestModel testMess) {
        return "OK dostalem odpowiedz gdzie testmess to - " + testMess.getTestMess();
    }
}
