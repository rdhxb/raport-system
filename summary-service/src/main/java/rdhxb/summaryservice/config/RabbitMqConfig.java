package rdhxb.summaryservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String REQUESTS_EXCHANGE = "summary_exchanger";
    public static final String REPLIES = "replies";
    public static final String QUEUE = "summary-service.request";

    @Bean
    public DirectExchange summaryExchanger(){
        return new DirectExchange(REQUESTS_EXCHANGE);
    }

    @Bean
    public Queue summaryQueue(){
        return new Queue(QUEUE);
    }

    @Bean
    public Binding binding(Queue summaryQueue, DirectExchange summaryExchanger){
        return BindingBuilder.bind(summaryQueue).to(summaryExchanger).with("summKey");
    }

    @Bean
    public MessageConverter messageConverter(){
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();

        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);

        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

}
