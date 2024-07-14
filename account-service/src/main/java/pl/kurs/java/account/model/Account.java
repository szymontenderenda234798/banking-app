package pl.kurs.java.account.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String accountNumber;
    private double plnBalance;
    private double usdBalance;

    public boolean exchange(String currencyFrom, String currencyTo, double amountFrom, double amountTo) {
        if (currencyFrom.equals("PLN") && plnBalance < amountFrom || currencyFrom.equals("USD") && usdBalance < amountFrom){
            return false;
        } else {
            if (currencyFrom.equals("PLN")) {
                plnBalance -= amountFrom;
                usdBalance += amountTo;
            } else {
                plnBalance += amountTo;
                usdBalance -= amountFrom;
            }
            return true;
        }
    }
}
