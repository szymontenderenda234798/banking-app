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
}
