package com.lin.monkey_finance.domain.transaction.service;

import com.lin.monkey_finance.BaseIntegrationTest;
//import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.ledger.dto.LedgerDetailedResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.transaction.dto.CategoryResponseDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionCreateDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionResponseDto;
import com.lin.monkey_finance.domain.transaction.model.TransactionType;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

public class TransactionIntegrationTest extends BaseIntegrationTest{

    @Autowired
    private TransactionService transactionService;

    private LedgerMembershipResponseDto membershipResponseDto;

    private CategoryResponseDto categoryResponseDto;

    @Autowired
    CategoryService categoryService;

    @BeforeEach
    void setUp(){
        UserResponseDto userResponseDto = super.createUser();
        LedgerDetailedResponseDto ledgerDetailedResponseDto = super.createDefaultLedger(userResponseDto.id());
        membershipResponseDto = super.createMembership(ledgerDetailedResponseDto.id(), userResponseDto.id());
//        AccountResponseDto accountResponseDto = super.createAccount(ledgerDetailedResponseDto.id(), userResponseDto.id());
        categoryResponseDto = categoryService.getSystemByName("Transfer");
    }

    @Test
    @DisplayName("Test transaction creation with a purposefully successful scenario")
    void testCreateTransaction(){
        TransactionCreateDto createDto = new TransactionCreateDto(
                "int ledger",
                "int description",
                BigDecimal.valueOf(999.99),
                Currency.USD,
                TransactionType.TRANSFER,
                OffsetDateTime.parse("2026-10-02T12:26:02+02:00"),
                categoryResponseDto.id(),
                null,
                null
        );

        TransactionResponseDto responseDto = transactionService.create(
                membershipResponseDto.ledgerId(),
                membershipResponseDto.userId(),
                createDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.id()).isNotNull();
        assertThat(responseDto.amount()).isEqualTo("999.99");
        assertThat(responseDto.currency()).isEqualTo(Currency.USD);
        assertThat(responseDto.type()).isEqualTo(TransactionType.TRANSFER);
        assertThat(responseDto.occurredAt()).isEqualTo(OffsetDateTime.parse("2026-10-02T12:26:02+02:00"));
        assertThat(responseDto.categoryId()).isEqualTo(categoryResponseDto.id());
        assertThat(responseDto.subcategoryId()).isNull();
        assertThat(responseDto.savingsPotId()).isNull();
    }
}
