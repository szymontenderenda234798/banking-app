package pl.kurs.java.exchange.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.springframework.stereotype.Service;
import pl.kurs.java.exchange.rabbitmq.RabbitMQConnectionFactory;

import java.io.IOException;

@Service
public class ExchangeService {
    private final static String QUEUE_NAME = "exchange_requests";

    public void exchange() {
        try (Connection connection = RabbitMQConnectionFactory.getConnection()) {
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "Exchange Request: USD to EUR, Amount: 100";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
