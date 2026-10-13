package rdhxb.raportservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import rdhxb.raportservice.config.RabbitMqConfig;
import rdhxb.raportservice.model.TestModel;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestService {

    private final RabbitTemplate rabbitTemplate;

    public void sendTestMess(String message){
//        create model object then send it to rabbitmq as a publisher
        TestModel test = new TestModel();
        test.setId(UUID.randomUUID());
        test.setTestMess(message);
        test.setTestInt(12);
        rabbitTemplate.convertAndSend(RabbitMqConfig.QUEUE,test);
    }

    @RabbitListener(queues = RabbitMqConfig.REPLIES)
    public void getReplies(String replies){
        System.out.println(replies);
    }

}
