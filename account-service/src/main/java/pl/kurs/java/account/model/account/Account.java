package pl.kurs.java.account.model.account;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;
import pl.kurs.java.account.exception.NoSubAccountInGivenCurrencyException;
import pl.kurs.java.account.model.transaction.enums.TransactionStatus;
import pl.kurs.java.account.model.transaction.request.BuyTransactionRequest;
import pl.kurs.java.account.model.transaction.request.SellTransactionRequest;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "pesel")})
public class Account {
    @Id
    @Column(unique = true, nullable = false)
    private String pesel;
    private String name;
    private String surname;
    private BigDecimal balance;
    private String currency;
    private String accountNumber;

    @OneToMany(mappedBy = "parentAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<SubAccount> subAccounts = new HashSet<>();

    public Account(String pesel, String name, String surname, BigDecimal balance, String currency, String accountNumber) {
        this.pesel = pesel;
        this.name = name;
        this.surname = surname;
        this.balance = balance;
        this.currency = currency;
        this.accountNumber = accountNumber;
    }

    public boolean addSubAccount(SubAccount subAccount) {
        subAccount.setParentAccount(this);
        return subAccounts.add(subAccount);
    }

    public boolean removeSubAccount(SubAccount subAccount) {
        subAccount.setParentAccount(null);
        return subAccounts.remove(subAccount);
    }

    public synchronized BuyTransactionRequest buyForeignCurrency(BuyTransactionRequest buyTransactionRequest) {
        if (balance.compareTo(buyTransactionRequest.amountToSpend()) < 0) {
            return new BuyTransactionRequest(buyTransactionRequest.pesel(), buyTransactionRequest.currencyToBuy(),
                    buyTransactionRequest.amountToBuy(), buyTransactionRequest.rate(), buyTransactionRequest.currencyToSpend(),
                    buyTransactionRequest.amountToSpend(), TransactionStatus.FAILED);
        }

        setBalance(balance.subtract(buyTransactionRequest.amountToSpend()));
        getSubAccounts().stream().filter(subAccount -> subAccount.getCurrency().equalsIgnoreCase(buyTransactionRequest.currencyToBuy()))
                .findFirst().ifPresent(subAccount -> subAccount.setBalance(subAccount.getBalance().add(buyTransactionRequest.amountToBuy())));

        return new BuyTransactionRequest(buyTransactionRequest.pesel(), buyTransactionRequest.currencyToBuy(),
                buyTransactionRequest.amountToBuy(), buyTransactionRequest.rate(), buyTransactionRequest.currencyToSpend(),
                buyTransactionRequest.amountToSpend(), TransactionStatus.COMPLETED);
    }

    public synchronized SellTransactionRequest sellForeignCurrency(SellTransactionRequest sellTransactionRequest) {
        SubAccount subAccount = getSubAccounts().stream().filter(sa -> sa.getCurrency().equalsIgnoreCase(sellTransactionRequest.currencyToSell()))
                .findFirst().orElseThrow(NoSubAccountInGivenCurrencyException::new);
        if (subAccount.getBalance().compareTo(sellTransactionRequest.amountToSell()) < 0) {
            return new SellTransactionRequest(sellTransactionRequest.pesel(), sellTransactionRequest.currencyToSell(),
                    sellTransactionRequest.amountToSell(), sellTransactionRequest.rate(), sellTransactionRequest.currencyToReceive(),
                    sellTransactionRequest.amountToReceive(), TransactionStatus.FAILED);
        }

        setBalance(balance.add(sellTransactionRequest.amountToReceive()));
        getSubAccounts().stream().filter(sa -> sa.getCurrency().equalsIgnoreCase(sellTransactionRequest.currencyToSell()))
                .findFirst().ifPresent(sa -> sa.setBalance(sa.getBalance().subtract(sellTransactionRequest.amountToSell())));

        return new SellTransactionRequest(sellTransactionRequest.pesel(), sellTransactionRequest.currencyToSell(),
                sellTransactionRequest.amountToSell(), sellTransactionRequest.rate(), sellTransactionRequest.currencyToReceive(),
                sellTransactionRequest.amountToReceive(), TransactionStatus.COMPLETED);
    }

    public boolean hasAccountInGivenCurrency(String currency) {
        return subAccounts.stream().anyMatch(subAccount -> subAccount.getCurrency().equalsIgnoreCase(currency));
    }


}
