package pl.kurs.java.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pl.kurs.java.exchange.config.MessageQueueConfig;
import pl.kurs.java.exchange.exception.PeselMismatchException;
import pl.kurs.java.exchange.model.ExchangeRequest;
import pl.kurs.java.exchange.model.RequestStatus;
import pl.kurs.java.exchange.model.command.CreateExchangeRequestCommand;
import pl.kurs.java.exchange.repository.ExchangeRepository;
import pl.kurs.java.exchange.service.provider.DateProvider;
import pl.kurs.java.client.model.ExchangeRequestDto;


@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final MessageQueueConfig messageQueueConfig;
    private final RabbitTemplate rabbitTemplate;
    private final ExchangeRepository exchangeRepository;
    private final DateProvider dateProvider;


    public void processExchangeRequestCommand(String pesel, CreateExchangeRequestCommand createExchangeRequestCommand) {
        if (!pesel.equals(createExchangeRequestCommand.pesel())) {
            throw new PeselMismatchException("PESEL_MISMATCH_FROM_PATH_VARIABLE_AND_REQUEST_BODY");
        }

        ExchangeRequest exchangeRequest = createExchangeRequest(createExchangeRequestCommand);
        exchangeRepository.save(exchangeRequest);
        postExchangeRequestDtoToQueue(exchangeRequest);
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

    private void postExchangeRequestDtoToQueue(ExchangeRequest exchangeRequest) {
        try {
            ExchangeRequestDto exchangeRequestDto = new ExchangeRequestDto(
                    exchangeRequest.getId(),
                    exchangeRequest.getPesel(),
                    exchangeRequest.getCurrencyFrom(),
                    exchangeRequest.getCurrencyTo(),
                    exchangeRequest.getAmountFrom(),
                    exchangeRequest.getAmountTo(),
                    exchangeRequest.getRate()
            );
            rabbitTemplate.convertAndSend(messageQueueConfig.getExchangeRequestExchangeName(), messageQueueConfig.getExchangeRequestRoutingKey(), exchangeRequestDto);
            System.out.println(" [x] Sent '" + exchangeRequest + "'");
            exchangeRequest.setStatus(RequestStatus.APPROVED);
        } catch (Exception e) {
            exchangeRequest.setStatus(RequestStatus.REJECTED);
            throw new RuntimeException("Failed to send message to the queue", e);
        } finally {
            exchangeRepository.save(exchangeRequest);
        }
    }
}
