package pl.kurs.java.account.repository;

import org.springframework.data.jpa.repository.Query;
import pl.kurs.java.account.model.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByAccountNumber(String accountNumber);
//    @Query("SELECT a FROM Account a WHERE a.pesel = :pesel")
    @Query("SELECT a FROM accounts a LEFT JOIN FETCH a.subAccounts WHERE a.pesel = :pesel")
    Optional<Account> findByPesel(String pesel);
}
