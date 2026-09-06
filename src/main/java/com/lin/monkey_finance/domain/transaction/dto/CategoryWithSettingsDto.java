package com.lin.monkey_finance.domain.transaction.dto;

import com.lin.monkey_finance.domain.transaction.model.Category;
import com.lin.monkey_finance.domain.transaction.model.CategorySettings;

public record CategoryWithSettingsDto(
        Category category,
        CategorySettings categorySettings
) {}
