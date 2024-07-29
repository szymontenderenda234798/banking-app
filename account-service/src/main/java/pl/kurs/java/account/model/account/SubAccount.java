package pl.kurs.java.account.model.account;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity(name = "sub_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal balance;
    private String currency;
    private String accountNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pesel")
    Account parentAccount;

    public SubAccount(BigDecimal balance, String currency, String accountNumber) {
        this.balance = balance;
        this.currency = currency;
        this.accountNumber = accountNumber;
    }


}
