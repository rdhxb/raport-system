package rdhxb.krsservice.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
//    same name as in producer
    public static final String QUEUE = "test-queue";



    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }
}
