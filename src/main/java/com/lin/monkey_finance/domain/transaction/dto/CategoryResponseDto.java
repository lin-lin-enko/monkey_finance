package com.lin.monkey_finance.domain.transaction.dto;

import com.lin.monkey_finance.domain.transaction.model.Category;
import com.lin.monkey_finance.domain.transaction.model.CategoryType;

import java.util.List;
import java.util.UUID;

public record CategoryResponseDto(
    UUID id,
    UUID ledgerId,
    UUID parentId,
    String name,
    String description,
    String fillColor,
    String fontColor,
    String iconUrl,
    boolean isSystem,
    CategoryType type,
    List<CategoryResponseDto> subcategories
) {}
