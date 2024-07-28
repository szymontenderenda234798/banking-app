package pl.kurs.java.account.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
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
}
