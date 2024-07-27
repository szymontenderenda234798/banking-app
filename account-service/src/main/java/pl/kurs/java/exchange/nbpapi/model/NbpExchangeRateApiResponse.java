package pl.kurs.java.exchange.nbpapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.util.List;

@Setter
public class NbpExchangeRateApiResponse {
    private String table;
    private String currency;
    private String code;
    private List<Rate> rates;

    @JsonProperty("table")
    public String getTable() {
        return table;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    @JsonProperty("rates")
    public List<Rate> getRates() {
        return rates;
    }

    @Setter
    public static class Rate {
        private String no;
        private String effectiveDate;
        private double mid;

        @JsonProperty("no")
        public String getNo() {
            return no;
        }

        @JsonProperty("effectiveDate")
        public String getEffectiveDate() {
            return effectiveDate;
        }

        @JsonProperty("mid")
        public double getMid() {
            return mid;
        }

    }
}
