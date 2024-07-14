package pl.kurs.java.account.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import pl.kurs.java.account.service.AccountService;
import pl.kurs.java.client.config.MessageQueueConfig;
import pl.kurs.java.client.config.properties.MessageQueueProperties;
import pl.kurs.java.client.model.ExchangeRequestDto;

@Service
@RequiredArgsConstructor
@Slf4j
@Import(pl.kurs.java.client.config.MessageQueueConfig.class)
public class ExchangeRequestListener {

    private final AccountService accountService;

    @RabbitListener(queues = MessageQueueProperties.EXCHANGE_REQUEST_QUEUE_NAME)
    public void receiveMessage(ExchangeRequestDto exchangeRequestDto) {
        accountService.processExchangeRequest(exchangeRequestDto);
    }
}
