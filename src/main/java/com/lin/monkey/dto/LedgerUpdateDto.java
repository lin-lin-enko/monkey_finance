package com.lin.monkey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LedgerUpdateDto {
    @Size(min = 3, max = 60, message = "Name must be 3 to 60 characters long")
    private String name;

    @Size(max = 255, message = "Description is too long")
    private String description;
    
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

}
