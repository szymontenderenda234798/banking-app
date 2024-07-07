package pl.kurs.java.account.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.kurs.java.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByPesel(String pesel);
}
