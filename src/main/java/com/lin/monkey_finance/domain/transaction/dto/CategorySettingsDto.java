package com.lin.monkey_finance.domain.transaction.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategorySettingsDto(

        @Size(min = 2, max = 20, message = "Custom name can only be 2 to 20 characters long")
        String customName,

        @Size(max = 128, message = "Custom description can't be longer than 128 characters")
        String customDescription,

        @Pattern(regexp = "^#[A-fa-f0-9]{6}$", message = "Custom fill color must be a valid hex color code")
        String customFillColor,

        @Pattern(regexp = "^#[A-fa-f0-9]{6}$", message = "Custom font color must be a valid hex color code")
        String customFontColor,

        @Size(max = 255, message = "Custom icon url can't be longer than 255 characters")
        String customIconUrl,

        Boolean isHidden
) {}
