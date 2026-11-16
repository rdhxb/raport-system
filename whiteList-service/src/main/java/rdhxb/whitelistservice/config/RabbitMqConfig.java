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
    public static final String DLQ = "wl-service.requests.dlq";
    public static final String DLX = "wl-dlx";
    public static final String DLQ_ROUTING_KEY = "wl-dl";
    @Bean
    public TopicExchange nipExchanger() {
        return new TopicExchange(NIP_EXCHANGER);
    }
    @Bean
    public Queue wlQueue(){
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue dlq(){
        return new Queue(DLQ);
    }

    @Bean
    public DirectExchange dlx(){
        return new DirectExchange(DLX);
    }

    @Bean
    public Binding dlBinding(DirectExchange dlx, Queue dlq){
        return BindingBuilder.bind(dlq).to(dlx).with(DLQ_ROUTING_KEY);
    }


    @Bean
    public Binding nipBinding(Queue wlQueue, TopicExchange nipExchanger){
        return BindingBuilder.bind(wlQueue).to(nipExchanger).with("nip.*");
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
