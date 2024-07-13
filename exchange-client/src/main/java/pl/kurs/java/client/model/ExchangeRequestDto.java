package pl.kurs.java.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long requestId;
    private String pesel;
    private String currencyFrom;
    private String currencyTo;
    private Double amountFrom;
    private Double amountTo;
    private Double rate;
}
