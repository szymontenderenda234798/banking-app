package pl.kurs.java.exchange.service.current_rate_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private String code;
    private Rate[] rates;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rate {
        private String code;
        private double mid;
    }

}