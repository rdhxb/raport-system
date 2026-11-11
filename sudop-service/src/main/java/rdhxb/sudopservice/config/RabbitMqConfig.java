package rdhxb.sudopservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String SUDOP_EXCHANGER = "sudop-exchanger";
    public static final String QUEUE = "sudop-service.requests";
    public static final String REPLIES = "replies";


    @Bean
    public DirectExchange sudopExchanger(){
        return new DirectExchange(SUDOP_EXCHANGER);
    }

    @Bean
    public Queue sudopQueue(){
        return new Queue(QUEUE);
    }

    @Bean
    public Binding sudopBinding(Queue sudopQueue, DirectExchange sudopExchanger){
        return BindingBuilder.bind(sudopQueue).to(sudopExchanger).with("");
    }

    @Bean
    public Queue repliesQueue(){
        return new Queue(REPLIES);
    }

    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }
}
