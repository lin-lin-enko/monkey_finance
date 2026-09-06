package com.lin.monkey_finance.domain.transaction.dto;

import com.lin.monkey_finance.domain.transaction.model.CategoryType;

import java.util.UUID;

public record CategoryResponseDto(
    UUID id,
    UUID ledgerId,
    String name,
    String description,
    String fillColor,
    String fontColor,
    String iconUrl,
    boolean isSystem,
    boolean isCustomized,
    CategoryType categoryType
) {}
