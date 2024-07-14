package pl.kurs.java.account.service.producer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import pl.kurs.java.client.config.MessageQueueConfig;
import pl.kurs.java.client.model.ExchangeResponseDto;

@Service
@RequiredArgsConstructor
@Import(pl.kurs.java.client.config.MessageQueueConfig.class)
public class ExchangeResponseProducer {

    private final MessageQueueConfig messageQueueConfig;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void postExchangeResponseDtoToQueue(ExchangeResponseDto exchangeResponseDto) {
        rabbitTemplate.convertAndSend(messageQueueConfig.getExchangeResponseExchangeName(), messageQueueConfig.getExchangeResponseRoutingKey(), exchangeResponseDto);
    }
}