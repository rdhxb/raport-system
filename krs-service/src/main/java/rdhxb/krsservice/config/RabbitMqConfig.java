package rdhxb.krsservice.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REQUESTS_EXCHANGE = "raport-requests";
    public static final String QUEUE = "krs-service.requests";
    public static final String REPLIES = "replies";
    public static final String DLQ = "krs-service.requests.dlq";
    public static final String DLX = "krs-dlx";
    public static final String DLQ_ROUTING_KEY = "krs-dl";

    @Bean
    public FanoutExchange requestExchange() {
        return new FanoutExchange(REQUESTS_EXCHANGE);
    }
    @Bean
    public Queue krsQueue(){
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
    public Binding krsBinding(Queue krsQueue, FanoutExchange requestExchange){
        return BindingBuilder.bind(krsQueue).to(requestExchange);
    }


    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }

}
