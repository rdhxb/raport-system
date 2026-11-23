package rdhxb.financeservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rdhxb.common.finance.FinanceRabbit;



@Configuration
public class RabbitMqConfig {

    public static final String REPLIES = "replies";
    public static final String QUEUE = "finance-service";

    @Bean
    public Queue financeQueue(){
        return new Queue(QUEUE);
    }
    @Bean
    public DirectExchange exchange(){
        return new DirectExchange(FinanceRabbit.EXCHANGE);
    }
    @Bean
    public Queue repliesQueue() {
        return new Queue(REPLIES);
    }
    @Bean
    public Binding bind(Queue financeQueue, DirectExchange exchange){
        return BindingBuilder.bind(financeQueue).to(exchange).with(FinanceRabbit.ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }
}
