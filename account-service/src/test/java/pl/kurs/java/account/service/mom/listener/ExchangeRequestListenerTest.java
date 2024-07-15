package pl.kurs.java.account.service.mom.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import pl.kurs.java.account.service.mom.producer.ExchangeResponseProducer;
import pl.kurs.java.client.config.MessageQueueConfig;
import pl.kurs.java.client.model.ExchangeResponseDto;
import pl.kurs.java.client.model.enums.ExchangeStatus;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExchangeResponseProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private MessageQueueConfig messageQueueConfig;

    @InjectMocks
    private ExchangeResponseProducer exchangeResponseProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void postExchangeResponseDtoToQueue_ShouldPostMessage() {
        // given
        ExchangeResponseDto exchangeResponseDto = new ExchangeResponseDto(1L, ExchangeStatus.COMPLETED);
        when(messageQueueConfig.getExchangeResponseExchangeName()).thenReturn("exchangeName");
        when(messageQueueConfig.getExchangeResponseRoutingKey()).thenReturn("routingKey");

        // when
        exchangeResponseProducer.postExchangeResponseDtoToQueue(exchangeResponseDto);

        // then
        verify(rabbitTemplate, times(1)).convertAndSend(eq("exchangeName"), eq("routingKey"), eq(exchangeResponseDto));
    }
}