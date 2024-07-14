package pl.kurs.java.exchange.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kurs.java.client.model.enums.ExchangeStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRequest implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pesel;
    private String currencyFrom;
    private String currencyTo;
    private Double amountFrom;
    private Double amountTo;
    private Double rate;
    private LocalDateTime requestTime;
    @Enumerated(EnumType.STRING)
    private ExchangeStatus status;

    public ExchangeRequest(String pesel, String currencyFrom, String currencyTo, Double amountFrom, Double amountTo, Double rate, LocalDateTime requestTime, ExchangeStatus status) {
        this.pesel = pesel;
        this.currencyFrom = currencyFrom;
        this.currencyTo = currencyTo;
        this.amountFrom = amountFrom;
        this.amountTo = amountTo;
        this.rate = rate;
        this.requestTime = requestTime;
        this.status = status;
    }
}
