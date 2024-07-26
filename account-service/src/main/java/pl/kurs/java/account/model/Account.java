package pl.kurs.java.account.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String accountNumber;
    @OneToMany(mappedBy = "parentAccount", cascade = CascadeType.ALL)
    private Set<SubAccount> subAccounts;
}
