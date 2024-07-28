package pl.kurs.java.account.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kurs.java.Main;
import pl.kurs.java.account.exception.AccountWithGivenPeselAlreadyExistsException;
import pl.kurs.java.account.exception.PeselFromPathVariableAndRequestBodyNotMatchingException;
import pl.kurs.java.account.model.account.Account;
import pl.kurs.java.account.model.account.SubAccount;
import pl.kurs.java.account.model.account.command.CreateAccountCommand;
import pl.kurs.java.account.model.account.command.UpdateAccountCommand;
import pl.kurs.java.account.model.account.dto.AccountDto;
import pl.kurs.java.account.repository.AccountRepository;
import pl.kurs.java.account.service.AccountService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;


@AutoConfigureMockMvc
@SpringBootTest(classes = Main.class)
@ActiveProfiles("test")
class AccountControllerTest {

    @SpyBean
    private AccountService accountService;

    @SpyBean
    private AccountRepository accountRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    private Statistics statistics;

    @PostConstruct
    public void init() {
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        sessionFactory.getStatistics().setStatisticsEnabled(true);
        statistics = sessionFactory.getStatistics();
    }

    @BeforeEach
    void clear() {
        reset(accountRepository);
        reset(accountService);
        accountRepository.deleteAll();
        statistics.clear();
    }

    @Test
    public void testGetAccount_ShouldGetAccount_WhenGivenValidPesel() throws Exception {
        // given
        Account account = prepareAccountsInDb().get(0);

        //when
        MvcResult result = mockMvc.perform(get("/api/v1/account/{pesel}", account.getPesel())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //then
        String response = result.getResponse().getContentAsString();
        AccountDto accountDto = objectMapper.readValue(response, AccountDto.class);

        assertAccountAndAccountDtoEquals(account, accountDto);

        verify(accountService, times(1)).getAccount(account.getPesel());
        verify(accountRepository, times(1)).findByPesel(account.getPesel());
    }

    @Test
    public void testGetAccount_ShouldReturnNotFound_WhenInvalidPesel() throws Exception {
        //when
        mockMvc.perform(get("/api/v1/account/{pesel}", "123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        //then
        verify(accountService, times(1)).getAccount(any());
        verify(accountRepository, times(1)).findByPesel(any());
    }


    @Test
    public void testGetAccounts_ShouldGetAllAcounts() throws Exception {
        //given
        List<Account> accounts = prepareAccountsInDb();

        //when
        MvcResult result = mockMvc.perform(get("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //then
        String response = result.getResponse().getContentAsString();

        JSONObject jsonObject = new JSONObject(response);
        JSONArray content = jsonObject.getJSONArray("content");

        assertEquals(2, content.length());

        JSONObject firstUser = content.getJSONObject(0);
        assertEquals(accounts.get(0).getPesel(), firstUser.getString("pesel"));
        JSONObject secondUser = content.getJSONObject(1);
        assertEquals(accounts.get(1).getPesel(), secondUser.getString("pesel"));
        JSONArray firstUserSubAccounts = firstUser.getJSONArray("subAccounts");
        assertEquals(accounts.get(0).getSubAccounts().size(), firstUserSubAccounts.length());

        verify(accountService, times(1)).getAccounts(any());
        verify(accountRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testCreateAccount_ShouldCreateAccount_WhenGivenValidData() throws Exception {
        // given
        CreateAccountCommand command = new CreateAccountCommand("08051129149", "John", "Doe", BigDecimal.valueOf(1000), "PLN");

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isCreated())
                .andReturn();

        //then
        String response = result.getResponse().getContentAsString();
        AccountDto accountDto = objectMapper.readValue(response, AccountDto.class);

        assertEquals(command.pesel(), accountDto.pesel());
        assertEquals(command.name(), accountDto.name());
        assertEquals(command.surname(), accountDto.surname());
        assertEquals(command.startingBalance(), accountDto.balance());
        assertEquals(command.currency(), accountDto.currency());
        verify(accountService, times(1)).createAccount(any(CreateAccountCommand.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_ShouldNotCreateAccount_WhenPeselIndicatesUnderage() throws Exception {
        // given
        CreateAccountCommand command = new CreateAccountCommand("14260486496", "John", "Doe", BigDecimal.valueOf(1000), "PLN");


        // when
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value("PESEL_NOT_ADULT"));

        //then
        verify(accountService, never()).createAccount(any(CreateAccountCommand.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_ShouldNotCreateAccount_WhenInvalidPeselFormat() throws Exception {
        // given
        CreateAccountCommand command = new CreateAccountCommand("12345", "John", "Doe", BigDecimal.valueOf(1000), "PLN");

        // when
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value("INVALID_PESEL_FORMAT"));

        //then
        verify(accountService, never()).createAccount(any(CreateAccountCommand.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_ShouldNotCreateAccount_WhenNameContainsNonLetterCharacters() throws Exception {
        // given
        CreateAccountCommand command = new CreateAccountCommand("08051129149", "John123", "Doe", BigDecimal.valueOf(1000), "PLN");

        // when
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value("INVALID_NAME_FORMAT_ONLY_LETTERS_ALLOWED"));

        //then
        verify(accountService, never()).createAccount(any(CreateAccountCommand.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_ShouldNotCreateAccount_WhenSurnameContainsNonLetterCharacters() throws Exception {
        // given
        CreateAccountCommand command = new CreateAccountCommand("08051129149", "John", "Doe123", BigDecimal.valueOf(1000), "PLN");

        // when
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value("INVALID_SURNAME_FORMAT_ONLY_LETTERS_ALLOWED"));

        //then
        verify(accountService, never()).createAccount(any(CreateAccountCommand.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_ShouldNotCreateAccount_WhenNegativeBalance() throws Exception {
        // given
        CreateAccountCommand command = new CreateAccountCommand("08051131748", "John", "Doe", BigDecimal.valueOf(-1000), "PLN");

        // when
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value("INVALID_BALANCE_NEGATIVE_VALUE"));

        //then
        verify(accountService, never()).createAccount(any(CreateAccountCommand.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_ShouldNotCreateAccount_WhenAccountWithGivenPeselExists() throws Exception {
        // given
        String existingPesel = "64060431778";
        CreateAccountCommand command = new CreateAccountCommand(existingPesel, "John", "Doe", BigDecimal.valueOf(1000), "PLN");
        String requestBody = asJsonString(command);
        when(accountRepository.findByPesel(existingPesel)).thenReturn(java.util.Optional.of(new Account()));

        // when
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(result -> assertInstanceOf(AccountWithGivenPeselAlreadyExistsException.class, result.getResolvedException()));

        // then
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testUpdateAccount_ShouldUpdateAccount_WhenGivenValidData() throws Exception {
        // given
        Account account = prepareAccountsInDb().get(0);
        UpdateAccountCommand command = new UpdateAccountCommand(account.getPesel(), "Tomek", "Romek");

        // when
        MvcResult result = mockMvc.perform(put("/api/v1/account/{pesel}", account.getPesel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String response = result.getResponse().getContentAsString();
        AccountDto accountDto = objectMapper.readValue(response, AccountDto.class);

        assertEquals(command.currentPesel(), accountDto.pesel());
        assertEquals(command.newName(), accountDto.name());
        assertEquals(command.newSurname(), accountDto.surname());
        assertNotEquals(account.getName(), accountDto.name());
        assertNotEquals(account.getSurname(), accountDto.surname());
        verify(accountService, times(1)).updateAccount(eq(account.getPesel()), any(UpdateAccountCommand.class));
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testUpdateAccount_ShouldNotUpdateAccount_WhenPeselDoesNotMatch() throws Exception {
        // given
        Account account = prepareAccountsInDb().get(0);
        UpdateAccountCommand command = new UpdateAccountCommand("wrongPesel", "Tomek", "Romek");

        // when
        mockMvc.perform(put("/api/v1/account/{pesel}", account.getPesel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertInstanceOf(PeselFromPathVariableAndRequestBodyNotMatchingException.class, result.getResolvedException()));

        // then
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testUpdateAccount_ShouldReturnNotFound_WhenAccountDoesNotExist() throws Exception {
        // given
        UpdateAccountCommand command = new UpdateAccountCommand("nonexistentPesel", "Tomek", "Romek");

        // when
        mockMvc.perform(put("/api/v1/account/{pesel}", "nonexistentPesel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isNotFound());

        // then
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testUpdateAccount_ShouldNotUpdateAccount_WhenNameContainsNonLetterCharacters() throws Exception {
        // given
        Account account = prepareAccountsInDb().get(0);
        UpdateAccountCommand command = new UpdateAccountCommand(account.getPesel(), "Tomek123", "Romek");

        // when
        mockMvc.perform(put("/api/v1/account/{pesel}", account.getPesel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value("INVALID_NAME_FORMAT_ONLY_LETTERS_ALLOWED"));

        // then
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testUpdateAccount_ShouldNotUpdateAccount_WhenSurnameContainsNonLetterCharacters() throws Exception {
        // given
        Account account = prepareAccountsInDb().get(0);
        UpdateAccountCommand command = new UpdateAccountCommand(account.getPesel(), "Tomek", "Romek123");

        // when
        mockMvc.perform(put("/api/v1/account/{pesel}", account.getPesel())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").value("INVALID_SURNAME_FORMAT_ONLY_LETTERS_ALLOWED"));

        // then
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testDeleteAccount_ShouldDeleteAccount_WhenGivenValidPesel() throws Exception {
        // given
        Account account = prepareAccountsInDb().get(0);

        // when
        mockMvc.perform(delete("/api/v1/account/{pesel}", account.getPesel())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // then
        verify(accountService, times(1)).deleteAccount(account.getPesel());
        verify(accountRepository, times(1)).delete(any(Account.class));
    }

    @Test
    void testDeleteAccount_ShouldReturnNotFound_WhenAccountDoesNotExist() throws Exception {
        // given
        String nonExistentPesel = "nonexistentPesel";

        // when
        mockMvc.perform(delete("/api/v1/account/{pesel}", nonExistentPesel)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // then
        verify(accountService, times(1)).deleteAccount(nonExistentPesel);
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    public void testGetAccounts_ShouldCountQuerries() throws Exception {
        //when
        mockMvc.perform(get("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        long executionCount = statistics.getQueryExecutionCount();
        assertEquals(1L, executionCount);

    }

    @Test
    public void testGetAccount_ShouldCountQuerries() throws Exception {
        //given
        Account account = prepareAccountsInDb().get(0);

        //when
        mockMvc.perform(get("/api/v1/account/{pesel}", account.getPesel())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        //then
        long executionCount = statistics.getQueryExecutionCount();
        assertEquals(1L, executionCount);
    }

    @Test
    public void testCreateAccount_ShouldCountQuerries() throws Exception {
        //given
        CreateAccountCommand createAccountCommand = new CreateAccountCommand("08051129149", "John", "Doe", BigDecimal.valueOf(1000), "PLN");

        //when
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createAccountCommand)))
                .andExpect(status().isCreated());

        //then
        long executionCount = statistics.getQueryExecutionCount();
        assertEquals(2L, executionCount);
    }

    @Test
    public void testUpdateAccount_ShouldCountQuerries() throws Exception {
        //given
        Account account = prepareAccountsInDb().get(0);
        UpdateAccountCommand updateAccountCommand = new UpdateAccountCommand(account.getPesel(), "tomek", "romek");

        //when
        mockMvc.perform(put("/api/v1/account/{pesel}", "12345678901")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updateAccountCommand)))
                .andExpect(status().isOk());

        //then
        long executionCount = statistics.getQueryExecutionCount();
        assertEquals(1L, executionCount);
    }

    @Test
    public void testDeleteAccount_ShouldCountQuerries() throws Exception {
        //given
        Account account = prepareAccountsInDb().get(0);

        //when
        mockMvc.perform(delete("/api/v1/account/{pesel}", account.getPesel())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        //then
        long executionCount = statistics.getQueryExecutionCount();
        assertEquals(1L, executionCount);
    }

    private List<Account> prepareAccountsInDb() {
        Account account1 = new Account("12345678901", "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");
        Account account2 = new Account("98765432109", "Jane", "Smith",BigDecimal.valueOf(500), "PLN", "65432109876543210987654321");
        SubAccount subAccount1 = new SubAccount(BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");
        account1.addSubAccount(subAccount1);
        List<Account> accounts = List.of(account1, account2);
        accountRepository.saveAll(accounts);
        return accounts;
    }

    private void assertAccountAndAccountDtoEquals(Account account, AccountDto accountDto) {
        assertEquals(account.getPesel(), accountDto.pesel());
        assertEquals(account.getName(), accountDto.name());
        assertEquals(account.getSurname(), accountDto.surname());
        assertEquals(0, account.getBalance().compareTo(accountDto.balance()));
        assertEquals(account.getCurrency(), accountDto.currency());
        assertEquals(account.getAccountNumber(), accountDto.accountNumber());
        assertEquals(account.getSubAccounts().size(), accountDto.subAccounts().size());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
