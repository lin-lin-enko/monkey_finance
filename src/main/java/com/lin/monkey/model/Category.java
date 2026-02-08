package com.lin.monkey.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Category must have a name")
    @Size(min = 3, max = 60, message = "Category name must be between 3 and 60 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(min = 10, max = 255, message = "Description must be 10 to 255 characters long if present")
    @Column(columnDefinition = "text")
    private String description;

    @Size(max = 255, message = "Icon URL can't be longer than 255 characters")
    @Column(name = "icon_url")
    private String iconUrl;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a hex code")
    @Column
    private String color = "#000000";

    public UUID getId() {
        return id;
    }

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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return id.equals(category.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
