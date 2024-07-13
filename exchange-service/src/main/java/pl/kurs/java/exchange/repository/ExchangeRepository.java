package pl.kurs.java.exchange.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.java.exchange.model.ExchangeRequest;

public interface ExchangeRepository extends JpaRepository<ExchangeRequest, Long> {
}
