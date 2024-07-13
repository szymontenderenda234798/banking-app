package pl.kurs.java.account.consumer;

import com.rabbitmq.client.*;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pl.kurs.java.account.rabbitmq.RabbitMQConnectionFactory;

import java.nio.charset.StandardCharsets;

@Component
public class AccountServiceConsumer {
    private final static String QUEUE_NAME = "exchange_requests";

    @RabbitListener(queues = QUEUE_NAME)
    public void consume(String message) {
        System.out.println(" [x] Received '" + message + "'");
        // Process the exchange request here
        processExchangeRequest(message);
    }

    private static void processExchangeRequest(String message) {
        // Logic to process the exchange request
        System.out.println("Processing exchange request: " + message);
        // Example logic: parse the message, update account balances, etc.
    }
}