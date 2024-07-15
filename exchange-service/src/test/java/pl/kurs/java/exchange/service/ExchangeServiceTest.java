package pl.kurs.java.exchange.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kurs.java.client.model.ExchangeResponseDto;
import pl.kurs.java.exchange.exception.ExchangeRequestNotFoundException;
import pl.kurs.java.exchange.exception.PeselMismatchException;
import pl.kurs.java.exchange.model.ExchangeRequest;
import pl.kurs.java.client.model.enums.ExchangeStatus;
import pl.kurs.java.exchange.model.command.CreateExchangeRequestCommand;
import pl.kurs.java.exchange.service.current_rate_api.CurrentRateApiCallerService;
import pl.kurs.java.exchange.repository.ExchangeRepository;
import pl.kurs.java.exchange.service.mom.producer.ExchangeRequestProducer;
import pl.kurs.java.exchange.service.provider.DateProvider;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExchangeServiceTest {

    @Mock
    private ExchangeRequestProducer exchangeRequestProducer;

    @Mock
    private ExchangeRepository exchangeRepository;

    @Mock
    private DateProvider dateProvider;

    @Mock
    private CurrentRateApiCallerService currentRateApiCallerService;

    @InjectMocks
    private ExchangeService exchangeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessExchangeRequestCommand_ShouldThrowPeselMismatchException_WhenPeselDoesNotMatch() {
        // given
        String pesel = "12345678901";
        CreateExchangeRequestCommand command = new CreateExchangeRequestCommand("98765432109", "PLN", "USD", 100.0);

        // when & then
        assertThrows(PeselMismatchException.class, () -> exchangeService.processExchangeRequestCommand(pesel, command));
    }

    @Test
    void testProcessExchangeRequestCommand_ShouldSaveExchangeRequest_WhenPeselMatches() {
        // given
        String pesel = "12345678901";
        CreateExchangeRequestCommand command = new CreateExchangeRequestCommand(pesel, "PLN", "USD", 100.0);
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        when(exchangeRequestProducer.postExchangeRequestDtoToQueue(any())).thenReturn(exchangeRequest);
        when(exchangeRepository.save(any(ExchangeRequest.class))).thenReturn(exchangeRequest);

        // when
        ExchangeRequest result = exchangeService.processExchangeRequestCommand(pesel, command);

        // then
        assertNotNull(result);
        verify(exchangeRequestProducer, times(1)).postExchangeRequestDtoToQueue(any(ExchangeRequest.class));
        verify(exchangeRepository, times(2)).save(any(ExchangeRequest.class));
    }

    @Test
    void testProcessExchangeResponseDto_ShouldUpdateStatus_WhenRequestExists() {
        // given
        ExchangeResponseDto responseDto = new ExchangeResponseDto();
        responseDto.setRequestId(1L);
        responseDto.setStatus(ExchangeStatus.APPROVED_FOR_PROCESSING);
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        when(exchangeRepository.findById(responseDto.getRequestId())).thenReturn(Optional.of(exchangeRequest));

        // when
        exchangeService.processExchangeResponseDto(responseDto);

        // then
        assertEquals(ExchangeStatus.APPROVED_FOR_PROCESSING, exchangeRequest.getStatus());
        verify(exchangeRepository, times(1)).save(exchangeRequest);
    }

    @Test
    void testProcessExchangeResponseDto_ShouldThrowExchangeRequestNotFoundException_WhenRequestDoesNotExist() {
        // given
        ExchangeResponseDto responseDto = new ExchangeResponseDto();
        responseDto.setRequestId(1L);
        when(exchangeRepository.findById(responseDto.getRequestId())).thenReturn(Optional.empty());

        // when & then
        assertThrows(ExchangeRequestNotFoundException.class, () -> exchangeService.processExchangeResponseDto(responseDto));
    }

    @Test
    void testGetCurrentExchangeRate_ShouldReturnInverseRate_WhenCurrencyFromIsPLN() {
        // given
        String currencyFrom = "PLN";
        String currencyTo = "USD";
        when(currentRateApiCallerService.getCurrentRate(currencyTo)).thenReturn(4.0);

        // when
        double rate = exchangeService.getCurrentExchangeRate(currencyFrom, currencyTo);

        // then
        assertEquals(0.25, rate);
        verify(currentRateApiCallerService, times(1)).getCurrentRate(currencyTo);
    }

    @Test
    void testGetCurrentExchangeRate_ShouldReturnDirectRate_WhenCurrencyFromIsNotPLN() {
        // given
        String currencyFrom = "USD";
        String currencyTo = "PLN";
        when(currentRateApiCallerService.getCurrentRate(currencyFrom)).thenReturn(4.0);

        // when
        double rate = exchangeService.getCurrentExchangeRate(currencyFrom, currencyTo);

        // then
        assertEquals(4.0, rate);
        verify(currentRateApiCallerService, times(1)).getCurrentRate(currencyFrom);
    }

    @Test
    void testCreateExchangeRequest_ShouldSaveExchangeRequest() {
        // given
        CreateExchangeRequestCommand command = new CreateExchangeRequestCommand("12345678901", "PLN", "USD", 100.0);
        ExchangeRequest exchangeRequest = new ExchangeRequest();
        when(exchangeRepository.save(any(ExchangeRequest.class))).thenReturn(exchangeRequest);

        // when
        ExchangeRequest result = exchangeService.createExchangeRequest(command);

        // then
        assertNotNull(result);
        verify(exchangeRepository, times(1)).save(any(ExchangeRequest.class));
    }

    @Test
    void testCreateExchangeRequestFromCommand_ShouldCreateExchangeRequest() {
        // given
        String pesel = "12345678901";
        String currencyFrom = "PLN";
        String currencyTo = "USD";
        double amount = 100.0;
        double rate = 0.25;
        CreateExchangeRequestCommand command = new CreateExchangeRequestCommand(pesel, currencyFrom, currencyTo, amount);
        when(dateProvider.provideNow()).thenReturn(LocalDateTime.now());
        when(currentRateApiCallerService.getCurrentRate(currencyTo)).thenReturn(4.0);

        // when
        ExchangeRequest exchangeRequest = exchangeService.createExchangeRequestFromCommand(command);

        // then
        assertNotNull(exchangeRequest);
        assertEquals(pesel, exchangeRequest.getPesel());
        assertEquals(currencyFrom, exchangeRequest.getCurrencyFrom());
        assertEquals(currencyTo, exchangeRequest.getCurrencyTo());
        assertEquals(amount, exchangeRequest.getAmountFrom());
        assertEquals(amount * rate, exchangeRequest.getAmountTo());
        assertEquals(rate, exchangeRequest.getRate());
        assertEquals(ExchangeStatus.PENDING, exchangeRequest.getStatus());
    }
}