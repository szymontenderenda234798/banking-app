package pl.kurs.java.exchange.service.producer;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import pl.kurs.java.client.config.MessageQueueConfig;
import pl.kurs.java.client.model.ExchangeRequestDto;
import pl.kurs.java.exchange.model.ExchangeRequest;
import pl.kurs.java.exchange.model.RequestStatus;

@Service
@RequiredArgsConstructor
@Import(pl.kurs.java.client.config.MessageQueueConfig.class)
public class ExchangeRequestProducer {

    private final MessageQueueConfig messageQueueConfig;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public ExchangeRequest postExchangeRequestDtoToQueue(ExchangeRequest exchangeRequest) {
        ExchangeRequestDto exchangeRequestDto = convertToExchangeRequestDto(exchangeRequest);
        try {
            rabbitTemplate.convertAndSend(messageQueueConfig.getExchangeRequestExchangeName(), messageQueueConfig.getExchangeRequestRoutingKey(), exchangeRequestDto);
            exchangeRequest.setStatus(RequestStatus.APPROVED_FOR_PROCESSING);
        } catch (Exception e) {
            exchangeRequest.setStatus(RequestStatus.ERROR_CONNECTING_TO_QUEUE);
        }
        return exchangeRequest;
    }

    private ExchangeRequestDto convertToExchangeRequestDto(ExchangeRequest exchangeRequest) {
        return new ExchangeRequestDto(
                exchangeRequest.getId(),
                exchangeRequest.getPesel(),
                exchangeRequest.getCurrencyFrom(),
                exchangeRequest.getCurrencyTo(),
                exchangeRequest.getAmountFrom(),
                exchangeRequest.getAmountTo(),
                exchangeRequest.getRate()
        );
    }
}
