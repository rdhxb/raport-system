package rdhxb.crbrservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REQUESTS_EXCHANGE = "raport-requests";
    public static final String QUEUE = "crbr-service.requests";
    public static final String REPLIES = "replies";
    public static final String DLQ = "crbr-service.requests.dlq";
    public static final String DLX = "crbr-dlx";
    public static final String DLQ_ROUTING_KEY = "crbr-dl";

    @Bean
    public FanoutExchange requestExchange() {
        return new FanoutExchange(REQUESTS_EXCHANGE);
    }
    @Bean
    public Queue crbrQueue(){
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
    public Binding dlBinding(Queue dlq, DirectExchange dlx){
        return BindingBuilder.bind(dlq).to(dlx).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Binding crbrBinding(Queue crbrQueue, FanoutExchange requestExchange){
        return BindingBuilder.bind(crbrQueue).to(requestExchange);
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
