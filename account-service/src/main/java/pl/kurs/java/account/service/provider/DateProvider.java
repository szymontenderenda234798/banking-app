package pl.kurs.java.account.service.provider;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DateProvider {
    public LocalDateTime provideNow() {
        return LocalDateTime.now();
    }

}
