package com.lin.monkey_finance.domain.ledger.dto;

import jakarta.validation.constraints.Size;

public record LedgerUpdateDto(
    @Size(min = 3, max = 25, message = "The name of the ledger must be 3 to 25 characters long")
    String name,
    @Size(max = 255, message = "Description can't be longer than 255 characters")
    String description
){}
