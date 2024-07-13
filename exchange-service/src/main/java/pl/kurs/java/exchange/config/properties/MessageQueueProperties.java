package pl.kurs.java.exchange.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rabbitmq.properties")
@Getter
@Setter
public class MessageQueueProperties {
    private String exchangeRequestQueueName;
    private String exchangeRequestRoutingKey;
    private String exchangeRequestExchangeName;
}
