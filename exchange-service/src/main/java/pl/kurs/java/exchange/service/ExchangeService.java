package pl.kurs.java.exchange.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.java.client.model.ExchangeResponseDto;
import pl.kurs.java.exchange.exception.ExchangeRequestNotFoundException;
import pl.kurs.java.exchange.exception.PeselMismatchException;
import pl.kurs.java.exchange.model.ExchangeRequest;
import pl.kurs.java.client.model.enums.ExchangeStatus;
import pl.kurs.java.exchange.model.command.CreateExchangeRequestCommand;
import pl.kurs.java.exchange.current_rate_api.service.CurrentRateApiCaller;
import pl.kurs.java.exchange.repository.ExchangeRepository;
import pl.kurs.java.exchange.service.mom.producer.ExchangeRequestProducer;
import pl.kurs.java.exchange.service.provider.DateProvider;


@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRequestProducer exchangeRequestProducer;
    private final ExchangeRepository exchangeRepository;
    private final DateProvider dateProvider;
    private final CurrentRateApiCaller currentRateApiCaller;

    @Transactional
    public ExchangeRequest processExchangeRequestCommand(String pesel, CreateExchangeRequestCommand createExchangeRequestCommand) {
        if (!pesel.equals(createExchangeRequestCommand.pesel())) {
            throw new PeselMismatchException();
        }
        ExchangeRequest exchangeRequest = exchangeRequestProducer.postExchangeRequestDtoToQueue(createExchangeRequest(createExchangeRequestCommand));
        return exchangeRepository.save(exchangeRequest);
    }

    @Transactional
    public void processExchangeResponseDto(ExchangeResponseDto exchangeResponseDto) {
        ExchangeRequest exchangeRequest = exchangeRepository.findById(exchangeResponseDto.getRequestId()).orElseThrow(ExchangeRequestNotFoundException::new);
        exchangeRequest.setStatus(exchangeResponseDto.getStatus());
        exchangeRepository.save(exchangeRequest);
    }

    private ExchangeRequest createExchangeRequest(CreateExchangeRequestCommand createExchangeRequestCommand) {
        return exchangeRepository.save(createExchangeRequestFromCommand(createExchangeRequestCommand));
    }

    private double getCurrentExchangeRate(String currencyFrom, String currencyTo) {
        if (currencyFrom.equals("PLN")) {
            return 1 / currentRateApiCaller.getCurrentRate(currencyTo);
        } else {
            return currentRateApiCaller.getCurrentRate(currencyFrom);
        }
    }

    private ExchangeRequest createExchangeRequestFromCommand(CreateExchangeRequestCommand createExchangeRequestCommand) {
        double rate = getCurrentExchangeRate(createExchangeRequestCommand.currencyFrom(), createExchangeRequestCommand.currencyTo());
        return new ExchangeRequest(
                createExchangeRequestCommand.pesel(),
                createExchangeRequestCommand.currencyFrom(),
                createExchangeRequestCommand.currencyTo(),
                createExchangeRequestCommand.amount(),
                createExchangeRequestCommand.amount() * rate,
                rate,
                dateProvider.provideNow(),
                ExchangeStatus.PENDING
        );
    }
}
