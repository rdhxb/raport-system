package rdhxb.whitelistservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String NIP_EXCHANGER = "nip-exchanger";
    public static final String QUEUE = "wl-service.requests";
    public static final String REPLIES = "replies";

    @Bean
    public TopicExchange nipExchanger() {
        return new TopicExchange(NIP_EXCHANGER);
    }
    @Bean
    public Queue nipQueue(){
        return new Queue(QUEUE);
    }

    @Bean
    public Binding nipBinding(Queue nipQueue, TopicExchange nipExchanger){
        return BindingBuilder.bind(nipQueue).to(nipExchanger).with("nip.*");
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
