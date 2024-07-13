package pl.kurs.java.exchange.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import pl.kurs.java.exchange.config.properties.MessageQueueProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;

@Configuration
@RequiredArgsConstructor
public class MessageQueueConfig {
    private final MessageQueueProperties properties;

    @Bean
    public Queue exchangeRequestQueue() {
        return new Queue(properties.getExchangeRequestQueueName(), true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(properties.getExchangeRequestExchangeName());
    }

    @Bean
    public Binding binding(Queue exchangeRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(exchangeRequestQueue).to(exchange).with(properties.getExchangeRequestRoutingKey());
    }

    public String getExchangeRequestQueueName() {
        return properties.getExchangeRequestQueueName();
    }

    public String getExchangeRequestRoutingKey() {
        return properties.getExchangeRequestRoutingKey();
    }

    public String getExchangeRequestExchangeName() {
        return properties.getExchangeRequestExchangeName();
    }
}
