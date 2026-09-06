package com.lin.monkey_finance.domain.transaction.dto;

import com.lin.monkey_finance.domain.transaction.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequestDto(
    @NotBlank(message = "Category name can't be blank")
    @Size(min = 2, max = 20, message = "Category name must be between 2 and 20 characters")
    String name,

    @Size(max = 128, message = "Description can't be longer than 128 characters")
    String description,

    @Pattern(regexp = "^#[A-fa-f0-9]{6}$", message = "Fill color must be a valid hex color code")
    String fillColor,

    @Pattern(regexp = "^#[A-fa-f0-9]{6}$", message = "Font color must be a valid hex color code")
    String fontColor,

    @Size(max = 255, message = "Icon url can't be longer than 255 characters")
    String iconUrl,

    @NotNull(message = "Category type can't be null")
    CategoryType type
) {}
