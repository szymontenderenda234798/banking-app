package pl.kurs.java.exchange.service.mom.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import pl.kurs.java.client.config.MessageQueueConfig;
import pl.kurs.java.client.model.ExchangeRequestDto;
import pl.kurs.java.exchange.model.ExchangeRequest;
import pl.kurs.java.client.model.enums.ExchangeStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExchangeRequestProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private MessageQueueConfig messageQueueConfig;

    @InjectMocks
    private ExchangeRequestProducer exchangeRequestProducer;

    private ExchangeRequest exchangeRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exchangeRequest = new ExchangeRequest();
        exchangeRequest.setId(1L);
        exchangeRequest.setPesel("12345678901");
        exchangeRequest.setCurrencyFrom("PLN");
        exchangeRequest.setCurrencyTo("USD");
        exchangeRequest.setAmountFrom(100.0);
        exchangeRequest.setAmountTo(25.0);
        exchangeRequest.setRate(0.25);
    }

    @Test
    void testPostExchangeRequestDtoToQueue_ShouldSetStatusApproved_WhenSendIsSuccessful() {
        // given
        when(messageQueueConfig.getExchangeRequestExchangeName()).thenReturn("exchangeName");
        when(messageQueueConfig.getExchangeRequestRoutingKey()).thenReturn("routingKey");

        // when
        ExchangeRequest result = exchangeRequestProducer.postExchangeRequestDtoToQueue(exchangeRequest);

        // then
        verify(rabbitTemplate, times(1)).convertAndSend(eq("exchangeName"), eq("routingKey"), any(ExchangeRequestDto.class));
        assertEquals(ExchangeStatus.APPROVED_FOR_PROCESSING, result.getStatus());
    }

    @Test
    void testPostExchangeRequestDtoToQueue_ShouldSetStatusError_WhenSendFails() {
        // given
        when(messageQueueConfig.getExchangeRequestExchangeName()).thenReturn("exchangeName");
        when(messageQueueConfig.getExchangeRequestRoutingKey()).thenReturn("routingKey");
        doThrow(new RuntimeException("Queue connection error")).when(rabbitTemplate).convertAndSend(eq("exchangeName"), eq("routingKey"), any(ExchangeRequestDto.class));

        // when
        ExchangeRequest result = exchangeRequestProducer.postExchangeRequestDtoToQueue(exchangeRequest);

        // then
        verify(rabbitTemplate, times(1)).convertAndSend(eq("exchangeName"), eq("routingKey"), any(ExchangeRequestDto.class));
        assertEquals(ExchangeStatus.ERROR_CONNECTING_TO_QUEUE, result.getStatus());
    }
}