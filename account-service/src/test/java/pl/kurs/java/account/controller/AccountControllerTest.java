package pl.kurs.java.account.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.kurs.java.account.Main;
import pl.kurs.java.account.exception.AccountNotFoundException;
import pl.kurs.java.account.model.dto.AccountDto;
import pl.kurs.java.account.repository.AccountRepository;
import pl.kurs.java.account.service.AccountService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldGetAllAccounts() throws Exception {
        // given
        AccountDto accountDto1 = new AccountDto("12345678901", "John", "Doe", "12345678901234567890123456", 1000.0, 0);
        AccountDto accountDto2 = new AccountDto("98765432109", "Jane", "Smith", "65432109876543210987654321", 2000.0, 500.0);
        List<AccountDto> content = List.of(accountDto1, accountDto2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<AccountDto> page = new PageImpl<>(content, pageable, content.size());
        when(accountRepository.findAll(pageable)).thenReturn(page);

        // when
        MvcResult result = mockMvc.perform(get("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        String response = result.getResponse().getContentAsString();
        PageDto<AccountDto> accounts = objectMapper.readValue(response, new TypeReference<>() {
        });

        verify(accountService, times(1)).getAccounts(any(Pageable.class));
        assertEquals(accounts.getContent().get(0), accountDto1);
        assertEquals(accounts.getContent().get(1), accountDto2);
    }


    @Test
    void testGetAccount_ShouldReturnAccount_WhenPeselIsValid() throws Exception {
        // given
        String pesel = "12345678901";
        AccountDto accountDto = new AccountDto(pesel, "John", "Doe", "accountNumber", 1000.0, 500.0);
        when(accountService.getAccount(pesel)).thenReturn(accountDto);

        // when & then
        mockMvc.perform(get("/api/v1/account/{pesel}", pesel)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pesel").value(pesel))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.accountNumber").value("accountNumber"))
                .andExpect(jsonPath("$.plnBalance").value(1000.0))
                .andExpect(jsonPath("$.usdBalance").value(500.0));

        verify(accountService, times(1)).getAccount(pesel);
    }

    @Test
    void testGetAccount_ShouldThrowAccountNotFoundException_WhenPeselDoesNotExist() throws Exception {
        // given
        String pesel = "12345678901";
        when(accountService.getAccount(pesel)).thenThrow(new AccountNotFoundException(pesel));

        // when & then
        mockMvc.perform(get("/api/v1/account/{pesel}", pesel)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccount(pesel);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public static class PageDto<T> {
        private List<T> content;
        private int number;
        private int size;
        private int totalPages;
        private long totalElements;
    }
}
