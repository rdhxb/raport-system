package rdhxb.raportservice.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REQUESTS_EXCHANGE = "raport-requests";
    public static final String NIP_EXCHANGER = "nip-exchanger";
    public static final String SUDOP_EXCHANGER = "sudop-exchanger";
    public static final String REPLIES = "replies";

    @Bean
    public FanoutExchange requestExchanger() {
        return new FanoutExchange(REQUESTS_EXCHANGE);
    }
    @Bean
    public TopicExchange nipExchanger(){
        return new TopicExchange(NIP_EXCHANGER);
    }

    @Bean
    public DirectExchange sudopExchanger(){
        return new DirectExchange(SUDOP_EXCHANGER);
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
