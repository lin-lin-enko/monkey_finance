package com.lin.monkey.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChangeDefaultLedgerDto(
        @JsonProperty("ledgerId")
        @NotNull(message = "ledgerId is required")
        UUID ledgerId
) {
}
