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
    public TopicExchange exchangeRequestExchange() {
        return new TopicExchange(MessageQueueProperties.EXCHANGE_REQUEST_EXCHANGE_NAME);
    }

    @Bean
    public Binding exchangeRequestBinding(Queue exchangeRequestQueue, TopicExchange exchangeRequestExchange) {
        return BindingBuilder.bind(exchangeRequestQueue).to(exchangeRequestExchange).with(MessageQueueProperties.EXCHANGE_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Queue exchangeResponseQueue() {
        return new Queue(MessageQueueProperties.EXCHANGE_RESPONSE_QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange exchangeResponseExchange() {
        return new TopicExchange(MessageQueueProperties.EXCHANGE_RESPONSE_EXCHANGE_NAME);
    }

    @Bean
    public Binding exchangeResponseBinding(Queue exchangeResponseQueue, TopicExchange exchangeResponseExchange) {
        return BindingBuilder.bind(exchangeResponseQueue).to(exchangeResponseExchange).with(MessageQueueProperties.EXCHANGE_RESPONSE_ROUTING_KEY);
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

    public String getExchangeResponseQueueName() {
        return MessageQueueProperties.EXCHANGE_RESPONSE_QUEUE_NAME;
    }

    public String getExchangeResponseRoutingKey() {
        return MessageQueueProperties.EXCHANGE_RESPONSE_ROUTING_KEY;
    }

    public String getExchangeResponseExchangeName() {
        return MessageQueueProperties.EXCHANGE_RESPONSE_EXCHANGE_NAME;
    }
}
