package com.lin.monkey_finance.domain.savings.service;

import com.lin.monkey_finance.TestcontainersConfiguration;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.account.service.AccountService;
import com.lin.monkey_finance.domain.auth.service.AuthService;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotCreateDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotResponseDto;
import com.lin.monkey_finance.domain.user.dto.UserRegisterDto;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.service.EmailService;
import com.lin.monkey_finance.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class SavingsPotServiceIntegrationTest {

    @Autowired
    private SavingsPotService savingsPotService;

    @Autowired
    private AuthService authService;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private LedgerMembershipService memberService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    UserResponseDto userResponseDto;

    LedgerMembershipResponseDto membershipResponseDto;

    AccountResponseDto accountResponseDto;

    @BeforeEach
    void setUp(){
        // setting test data

        UserRegisterDto testUserRegisterDto = new UserRegisterDto(
                "testusername",
                "testemail@mail.com",
                "password",
                "Test Name",
                LocalDate.of(2008, 4, 28)
        );

        if(userService.existsByEmail("testemail@mail.com"))
            userResponseDto = userService.getByEmail("testemail@mail.com");
        else userResponseDto = authService.register(testUserRegisterDto);

        membershipResponseDto = memberService.getUserDefaultLedgerMembership(userResponseDto.id());

        accountResponseDto = accountService.getLedgerAccount(membershipResponseDto.userId(), membershipResponseDto.ledgerId());
    }

    @Test
    @DisplayName("Test if savings creation works")
    void createSavings(){
        SavingsPotCreateDto createDto = new SavingsPotCreateDto(
                "Dog",
                "SavingsPot pot for buying a borzoi doggo",
                Currency.UAH,
                new BigDecimal(500),
                null,
                null
        );

        SavingsPotResponseDto responseDto = savingsPotService.create(membershipResponseDto.userId(), accountResponseDto.id(), createDto);
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isNotNull();
        assertThat(responseDto.name()).isEqualTo(createDto.name());
        assertThat(responseDto.description()).isEqualTo(createDto.description());
        assertThat(responseDto.currency()).isEqualTo(createDto.currency());
        assertThat(responseDto.targetAmount()).isEqualByComparingTo(createDto.targetAmount());
    }

    @Test
    @DisplayName("Test if exceptions are caught when creating a savings pot")
    void testExceptionCatchOnSavingsCreation(){
        SavingsPotCreateDto createDto = new SavingsPotCreateDto(
                null,
                null,
               null,
                null,
                null,
                null
        );

        assertThatThrownBy(() ->
                savingsPotService.create(membershipResponseDto.userId(), accountResponseDto.id(), createDto)
        )
        .isInstanceOf(DataIntegrityViolationException.class);
    }
}
