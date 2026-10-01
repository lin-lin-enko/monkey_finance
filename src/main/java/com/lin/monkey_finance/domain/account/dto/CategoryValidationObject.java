package com.lin.monkey_finance.domain.account.dto;

import com.lin.monkey_finance.domain.transaction.model.Category;

public record CategoryValidationObject(
    Category category,
    Category subcategory
){}
