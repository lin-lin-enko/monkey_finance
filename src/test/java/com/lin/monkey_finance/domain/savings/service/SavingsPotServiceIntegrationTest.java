package com.lin.monkey_finance.domain.savings.service;

import com.lin.monkey_finance.BaseIntegrationTest;
import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.ledger.dto.LedgerDetailedResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotCreateDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotResponseDto;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

public class SavingsPotServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SavingsPotService savingsPotService;

    LedgerMembershipResponseDto membershipResponseDto;

    @BeforeEach
    void setUp(){
        UserResponseDto userResponseDto = super.createUser();
        LedgerDetailedResponseDto ledgerDetailedResponseDto = super.createDefaultLedger(userResponseDto.id());
        membershipResponseDto = createMembership(ledgerDetailedResponseDto.id(), userResponseDto.id());
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

        SavingsPotResponseDto responseDto = savingsPotService.create(membershipResponseDto.ledgerId(), membershipResponseDto.userId(), createDto);
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
                savingsPotService.create(membershipResponseDto.ledgerId(), membershipResponseDto.userId(), createDto)
        )
        .isInstanceOf(DataIntegrityViolationException.class);
    }
}
