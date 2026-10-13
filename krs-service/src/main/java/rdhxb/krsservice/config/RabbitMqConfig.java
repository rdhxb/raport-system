package rdhxb.krsservice.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
//    same name as in producer
    public static final String QUEUE = "test-queue";
    public static final String REPLIES = "replies";

    @Bean
    public Queue testQueue() {
        return new Queue(QUEUE);
    }

    @Bean
    public Queue repliesQueue() {
        return new Queue(REPLIES);
    }

    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }

}
