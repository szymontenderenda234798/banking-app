package pl.kurs.java.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kurs.java.account.model.SubAccount;

public interface SubAccountRepository extends JpaRepository<SubAccount, String> {
}
