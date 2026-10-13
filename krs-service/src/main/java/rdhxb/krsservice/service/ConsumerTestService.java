package rdhxb.krsservice.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import rdhxb.krsservice.config.RabbitMqConfig;
import rdhxb.krsservice.model.TestModel;

@Service
public class ConsumerTestService {

    @RabbitListener(queues = RabbitMqConfig.QUEUE)
    public TestModel processTestMess(TestModel testMess) {
        return testMess;
    }
}
