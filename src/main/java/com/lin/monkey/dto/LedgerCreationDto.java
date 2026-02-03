package com.lin.monkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LedgerCreationDto {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 60, message = "Name must be 3 to 60 characters long")
    private String name;

    @Size(max = 255, message = "Description is too long")
    private String description;

    private boolean isDefault = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean newIsDefault) {
        isDefault = newIsDefault;
    }
}
