package com.lin.monkey_finance;

import com.lin.monkey_finance.domain.account.dto.AccountCreateDto;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.AccountType;
import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.account.service.AccountService;
import com.lin.monkey_finance.domain.ledger.dto.LedgerCreateDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerDetailedResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.mapper.UserMapper;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.model.UserStatus;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@ActiveProfiles("test")
// tells spring to up the whole app for a test
@SpringBootTest
// starts real docker containers before testing
@Import(TestcontainersConfiguration.class)
public abstract class BaseIntegrationTest {
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected LedgerService ledgerService;
    @Autowired
    protected LedgerMembershipService membershipService;
    @Autowired
    protected AccountService accountService;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected UserResponseDto createUser(){
        String randomId = UUID.randomUUID().toString().substring(0, 5);
        User user = new User(
                "testusername-" + randomId,
                "testemail-" + randomId + "@mail.com",
                passwordEncoder.encode("password"),
                "Test Name",
                LocalDate.of(1999, 4, 28),
                UserStatus.ACTIVE
        );
        return userMapper.toResponseDto(userRepository.save(user));
    }

    protected LedgerDetailedResponseDto createDefaultLedger(UUID userId){
        LedgerCreateDto ledgerCreateDto = new LedgerCreateDto(
                "test ledger",
                "test description"
        );
        return ledgerService.create(userId, ledgerCreateDto);
    }

    protected LedgerMembershipResponseDto createMembership(UUID ledgerId, UUID userId){
        membershipService.addByRegistration(ledgerId, userId);
        return membershipService.getUserDefaultLedgerMembership(userId);
    }

    protected AccountResponseDto createAccount(UUID ledgerId, UUID userId){
        AccountCreateDto accountCreateDto = new AccountCreateDto(
                "test account",
                AccountType.BANK,
                "Test institution",
                BigDecimal.valueOf(100.34),
                Currency.EUR,
                "test description"
        );
        return accountService.create(ledgerId, userId, accountCreateDto);
    }
}
