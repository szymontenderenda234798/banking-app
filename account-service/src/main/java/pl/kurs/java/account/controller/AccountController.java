package pl.kurs.java.account.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import pl.kurs.java.account.model.command.CreateAccountCommand;
import pl.kurs.java.account.model.command.UpdateAccountCommand;
import pl.kurs.java.account.model.dto.AccountDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.kurs.java.account.service.AccountService;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<Page<AccountDto>> getAccounts(@PageableDefault Pageable pageable) {
        log.info("Getting all accounts");
        return ResponseEntity.ok(accountService.getAccounts(pageable));
    }

    @GetMapping("/{pesel}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable String pesel) {
        log.info("Getting account with pesel: {}", pesel);
        return ResponseEntity.ok(accountService.getAccount(pesel));
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody @Valid CreateAccountCommand createAccountCommand) {
        log.info("Adding account for: {} {}", createAccountCommand.name(), createAccountCommand.surname());
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(createAccountCommand));
    }

    @PutMapping("/{pesel}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable String pesel, @RequestBody @Valid UpdateAccountCommand updateAccountCommand) {
        log.info("Updating account with pesel: {}", pesel);
        return ResponseEntity.ok(accountService.updateAccount(pesel, updateAccountCommand));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(@PathVariable String pesel) {
        log.info("Deleting account with pesel: {}", pesel);
        accountService.deleteAccount(pesel);
        return ResponseEntity.noContent().build();
    }
}
