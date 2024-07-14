package pl.kurs.java.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.kurs.java.client.model.enums.ExchangeStatus;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ExchangeResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long requestId;
    private ExchangeStatus status;
}
