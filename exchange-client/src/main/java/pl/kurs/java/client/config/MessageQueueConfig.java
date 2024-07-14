package pl.kurs.java.client.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.kurs.java.client.config.properties.MessageQueueProperties;

@Configuration
@RequiredArgsConstructor
public class MessageQueueConfig {

    @Bean
    public Queue exchangeRequestQueue() {
        return new Queue(MessageQueueProperties.EXCHANGE_REQUEST_QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(MessageQueueProperties.EXCHANGE_REQUEST_EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue exchangeRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(exchangeRequestQueue).to(exchange).with(MessageQueueProperties.EXCHANGE_REQUEST_ROUTING_KEY);
    }

    public String getExchangeRequestQueueName() {
        return MessageQueueProperties.EXCHANGE_REQUEST_QUEUE_NAME;
    }

    public String getExchangeRequestRoutingKey() {
        return MessageQueueProperties.EXCHANGE_REQUEST_ROUTING_KEY;
    }

    public String getExchangeRequestExchangeName() {
        return MessageQueueProperties.EXCHANGE_REQUEST_EXCHANGE_NAME;
    }
}
