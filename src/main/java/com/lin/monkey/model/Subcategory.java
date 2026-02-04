package com.lin.monkey.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "subcategories")
public class Subcategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false, nullable = false)
    private Category category;

    @NotBlank(message = "Subcategory must have a name")
    @Size(min = 3, max = 60, message = "Subcategory name must be 3 to 60 characters long")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(min = 30, max = 255, message = "Description must be 30 to 255 characters long if present")
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

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategory(Category category) {
        this.category = category;
        this.categoryId = (category != null) ? category.getId() : null;
    }

    public Category getCategory() {
        return category;
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
        Subcategory subcategory = (Subcategory) obj;
        return id.equals(subcategory.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
