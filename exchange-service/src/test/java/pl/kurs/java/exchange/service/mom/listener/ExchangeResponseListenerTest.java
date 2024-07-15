package pl.kurs.java.exchange.service.mom.listener;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kurs.java.client.model.ExchangeResponseDto;
import pl.kurs.java.client.model.enums.ExchangeStatus;
import pl.kurs.java.exchange.service.ExchangeService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ExchangeResponseListenerTest {

    @Mock
    private ExchangeService exchangeService;

    @InjectMocks
    private ExchangeResponseListener exchangeResponseListener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testReceiveMessage_ShouldProcessExchangeResponseDto() {
        // given
        ExchangeResponseDto exchangeResponseDto = new ExchangeResponseDto();
        exchangeResponseDto.setRequestId(1L);
        exchangeResponseDto.setStatus(ExchangeStatus.APPROVED_FOR_PROCESSING);

        // when
        exchangeResponseListener.receiveMessage(exchangeResponseDto);

        // then
        verify(exchangeService, times(1)).processExchangeResponseDto(exchangeResponseDto);
    }
}