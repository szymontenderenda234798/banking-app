package pl.kurs.java.exchange.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kurs.java.exchange.Main;
import pl.kurs.java.exchange.service.ExchangeService;
import pl.kurs.java.exchange.service.current_rate_api.CurrentRateApiCallerService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Main.class)
@AutoConfigureMockMvc
class ExchangeControllerTest {

    @SpyBean
    private ExchangeService exchangeService;

    @SpyBean
    private CurrentRateApiCallerService currentRateApiCallerService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private Statistics statistics;

    @PostConstruct
    public void init() {
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        sessionFactory.getStatistics().setStatisticsEnabled(true);
        statistics = sessionFactory.getStatistics();
    }

    @BeforeEach
    void clear() {
        reset(exchangeService);
        reset(currentRateApiCallerService);
        statistics.clear();
    }

    @Test
    void testGetCurrentRate_ShouldReturnRate_WhenCurrencyIsValid() throws Exception {
        // given
        String currency = "USD";
        double rate = 3.5;
        when(currentRateApiCallerService.getCurrentRate(currency)).thenReturn(rate);

        // when
        MvcResult result = mockMvc.perform(get("/api/v1/exchange/{currency}", currency)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String response = result.getResponse().getContentAsString();
        double responseRate = objectMapper.readValue(response, Double.class);

        assertEquals(rate, responseRate);
        verify(currentRateApiCallerService, times(1)).getCurrentRate(currency);
    }

    @Test
    void testGetCurrentRate_ShouldReturnNotFound_WhenCurrencyIsInvalid() throws Exception {
        // given
        String currency = "INVALID";

        // when & then
        mockMvc.perform(get("/api/v1/exchange/{currency}", currency)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(currentRateApiCallerService, times(1)).getCurrentRate(currency);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}