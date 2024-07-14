package pl.kurs.java.exchange.service.mom.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import pl.kurs.java.client.config.properties.MessageQueueProperties;
import pl.kurs.java.client.model.ExchangeResponseDto;
import pl.kurs.java.exchange.service.ExchangeService;

@Service
@RequiredArgsConstructor
@Slf4j
@Import(pl.kurs.java.client.config.MessageQueueConfig.class)
public class ExchangeResponseListener {

    private final ExchangeService exchangeService;

    @RabbitListener(queues = MessageQueueProperties.EXCHANGE_RESPONSE_QUEUE_NAME)
    public void receiveMessage(ExchangeResponseDto exchangeResponseDto) {
        exchangeService.processExchangeResponseDto(exchangeResponseDto);
    }
}
