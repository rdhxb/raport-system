package rdhxb.krsservice.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REQUESTS_EXCHANGE = "raport-requests";
    public static final String QUEUE = "krs-service.requests";
    public static final String REPLIES = "replies";

    @Bean
    public FanoutExchange requestExchange() {
        return new FanoutExchange(REQUESTS_EXCHANGE);
    }
    @Bean
    public Queue krsQueue(){
        return new Queue(QUEUE);
    }

    @Bean
    public Binding krsBinding(Queue krsQueue, FanoutExchange requestExchange){
        return BindingBuilder.bind(krsQueue).to(requestExchange);
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
