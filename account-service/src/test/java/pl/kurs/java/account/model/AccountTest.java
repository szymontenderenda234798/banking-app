package pl.kurs.java.account.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account("12345678901", "John", "Doe", "12345678901234567890123456", 1000.0, 500.0);
    }

    @Test
    void testExchange_ShouldSucceed_WhenPlnToUsdWithSufficientBalance() {
        boolean result = account.exchange("PLN", "USD", 100.0, 25.0);
        assertTrue(result);
        assertEquals(900.0, account.getPlnBalance());
        assertEquals(525.0, account.getUsdBalance());
    }

    @Test
    void testExchange_ShouldSucceed_WhenUsdToPlnWithSufficientBalance() {
        boolean result = account.exchange("USD", "PLN", 100.0, 400.0);
        assertTrue(result);
        assertEquals(1400.0, account.getPlnBalance());
        assertEquals(400.0, account.getUsdBalance());
    }

    @Test
    void testExchange_ShouldFail_WhenPlnToUsdWithInsufficientBalance() {
        boolean result = account.exchange("PLN", "USD", 2000.0, 500.0);
        assertFalse(result);
        assertEquals(1000.0, account.getPlnBalance());
        assertEquals(500.0, account.getUsdBalance());
    }

    @Test
    void testExchange_ShouldFail_WhenUsdToPlnWithInsufficientBalance() {
        boolean result = account.exchange("USD", "PLN", 1000.0, 4000.0);
        assertFalse(result);
        assertEquals(1000.0, account.getPlnBalance());
        assertEquals(500.0, account.getUsdBalance());
    }

    @Test
    void testExchange_ShouldNotAffectBalances_WhenPlnToUsdFailsDueToInsufficientBalance() {
        boolean result = account.exchange("PLN", "USD", 2000.0, 500.0);
        assertFalse(result);
        assertEquals(1000.0, account.getPlnBalance());
        assertEquals(500.0, account.getUsdBalance());
    }

    @Test
    void testExchange_ShouldNotAffectBalances_WhenUsdToPlnFailsDueToInsufficientBalance() {
        boolean result = account.exchange("USD", "PLN", 1000.0, 4000.0);
        assertFalse(result);
        assertEquals(1000.0, account.getPlnBalance());
        assertEquals(500.0, account.getUsdBalance());
    }

    @Test
    void testExchange_ShouldSucceed_WhenPlnToUsdWithExactBalance() {
        account.setPlnBalance(100.0);
        boolean result = account.exchange("PLN", "USD", 100.0, 25.0);
        assertTrue(result);
        assertEquals(0.0, account.getPlnBalance());
        assertEquals(525.0, account.getUsdBalance());
    }

    @Test
    void testExchange_ShouldSucceed_WhenUsdToPlnWithExactBalance() {
        account.setUsdBalance(100.0);
        boolean result = account.exchange("USD", "PLN", 100.0, 400.0);
        assertTrue(result);
        assertEquals(1400.0, account.getPlnBalance());
        assertEquals(0.0, account.getUsdBalance());
    }

    @Test
    void testExchange_ShouldFail_WhenInvalidCurrencyFrom() {
        boolean result = account.exchange("EUR", "USD", 100.0, 25.0);
        assertFalse(result);
        assertEquals(1000.0, account.getPlnBalance());
        assertEquals(500.0, account.getUsdBalance());
    }

    @Test
    void testExchange_ShouldFail_WhenInvalidCurrencyTo() {
        boolean result = account.exchange("PLN", "EUR", 100.0, 25.0);
        assertFalse(result);
        assertEquals(1000.0, account.getPlnBalance());
        assertEquals(500.0, account.getUsdBalance());
    }
}