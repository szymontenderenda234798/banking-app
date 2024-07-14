package pl.kurs.java.exchange.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.java.exchange.exception.PeselMismatchException;
import pl.kurs.java.exchange.model.ExchangeRequest;
import pl.kurs.java.exchange.model.RequestStatus;
import pl.kurs.java.exchange.model.command.CreateExchangeRequestCommand;
import pl.kurs.java.exchange.repository.ExchangeRepository;
import pl.kurs.java.exchange.service.producer.ExchangeRequestProducer;
import pl.kurs.java.exchange.service.provider.DateProvider;


@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRequestProducer exchangeRequestProducer;
    private final ExchangeRepository exchangeRepository;
    private final DateProvider dateProvider;

    @Transactional
    public void processExchangeRequestCommand(String pesel, CreateExchangeRequestCommand createExchangeRequestCommand) {
        if (!pesel.equals(createExchangeRequestCommand.pesel())) {
            throw new PeselMismatchException();
        }
        ExchangeRequest exchangeRequest = exchangeRequestProducer.postExchangeRequestDtoToQueue(createExchangeRequest(createExchangeRequestCommand));
        exchangeRepository.save(exchangeRequest);
    }

    private ExchangeRequest createExchangeRequest(CreateExchangeRequestCommand createExchangeRequestCommand) {
        double rate = getCurrentExchangeRate(createExchangeRequestCommand.currencyFrom(), createExchangeRequestCommand.currencyTo());
        return new ExchangeRequest(
                createExchangeRequestCommand.pesel(),
                createExchangeRequestCommand.currencyFrom(),
                createExchangeRequestCommand.currencyTo(),
                createExchangeRequestCommand.amount(),
                createExchangeRequestCommand.amount() * rate,
                rate,
                dateProvider.provideNow(),
                RequestStatus.PENDING
        );
    }

    private double getCurrentExchangeRate(String currencyFrom, String currencyTo) {
        //TODO:
        // some logic to get the current exchange rate
        return 4.0;
    }
}
