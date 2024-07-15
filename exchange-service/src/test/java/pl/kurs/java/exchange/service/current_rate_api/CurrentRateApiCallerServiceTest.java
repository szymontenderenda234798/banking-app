package pl.kurs.java.exchange.service.current_rate_api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.kurs.java.exchange.exception.ErrorDuringGetCurrentRateApiCallException;
import pl.kurs.java.exchange.service.current_rate_api.model.ApiResponse;

class CurrentRateApiCallerServiceTest {

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CurrentRateApiCallerService currentRateApiCallerService;

    private static final String VALID_CURRENCY = "usd";
    private static final String VALID_API_RESPONSE = "{ \"code\": \"USD\", \"rates\": [{ \"mid\": 3.9099 }] }";
    private static final String EMPTY_API_RESPONSE = "{ \"code\": \"USD\", \"rates\": [] }";
    private static final String MALFORMED_API_RESPONSE = "{ \"code\": \"USD\", \"rates\": [{ \"mid\": }";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCurrentRate_ShouldReturnRate_WhenApiCallIsSuccessful() throws Exception {
        // given
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(VALID_API_RESPONSE);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setRates(new ApiResponse.Rate[] { new ApiResponse.Rate() });
        apiResponse.getRates()[0].setMid(3.9099);
        when(objectMapper.readValue(VALID_API_RESPONSE, ApiResponse.class)).thenReturn(apiResponse);

        // when
        double rate = currentRateApiCallerService.getCurrentRate(VALID_CURRENCY);

        // then
        assertEquals(3.9099, rate);
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetCurrentRate_ShouldThrowException_WhenApiResponseIsInvalid() throws Exception {
        // given
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(EMPTY_API_RESPONSE);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setRates(new ApiResponse.Rate[0]);
        when(objectMapper.readValue(EMPTY_API_RESPONSE, ApiResponse.class)).thenReturn(apiResponse);

        // when & then
        assertThrows(ErrorDuringGetCurrentRateApiCallException.class, () -> currentRateApiCallerService.getCurrentRate(VALID_CURRENCY));
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetCurrentRate_ShouldThrowException_WhenApiCallFails() throws Exception {
        // given
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(IOException.class);

        // when & then
        assertThrows(ErrorDuringGetCurrentRateApiCallException.class, () -> currentRateApiCallerService.getCurrentRate(VALID_CURRENCY));
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetCurrentRate_ShouldThrowException_WhenApiReturnsNon200Status() throws Exception {
        // given
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(404);

        // when & then
        assertThrows(ErrorDuringGetCurrentRateApiCallException.class, () -> currentRateApiCallerService.getCurrentRate(VALID_CURRENCY));
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetCurrentRate_ShouldThrowException_WhenInterruptedExceptionOccurs() throws Exception {
        // given
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenThrow(InterruptedException.class);

        // when & then
        assertThrows(ErrorDuringGetCurrentRateApiCallException.class, () -> currentRateApiCallerService.getCurrentRate(VALID_CURRENCY));
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGetCurrentRate_ShouldThrowException_WhenEmptyBodyReceived() throws Exception {
        // given
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("");

        // when & then
        assertThrows(ErrorDuringGetCurrentRateApiCallException.class, () -> currentRateApiCallerService.getCurrentRate(VALID_CURRENCY));
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}

