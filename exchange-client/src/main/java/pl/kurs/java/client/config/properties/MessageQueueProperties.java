package pl.kurs.java.client.config.properties;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MessageQueueProperties {
    public static final String EXCHANGE_REQUEST_QUEUE_NAME = "exchange-request-queue";
    public static final String EXCHANGE_REQUEST_ROUTING_KEY = "exchange-request-routing-key";
    public static final String EXCHANGE_REQUEST_EXCHANGE_NAME = "exchange-request-exchange";

    public static final String EXCHANGE_RESPONSE_QUEUE_NAME = "exchange-response-queue";
    public static final String EXCHANGE_RESPONSE_ROUTING_KEY = "exchange-response-routing-key";
    public static final String EXCHANGE_RESPONSE_EXCHANGE_NAME = "exchange-response-exchange";
}
