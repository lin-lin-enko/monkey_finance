package com.lin.monkey_finance.domain.transaction.model;

import com.lin.monkey_finance.domain.ledger.model.Ledger;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Entity
@Table(name = "categories", schema = "dev")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @JoinColumn(name = "ledger_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Ledger ledger;

    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    @Column(nullable = false, length = 50)
    private String name;

    @Size(max = 128, message = "Description can't be longer than 255 characters")
    @Column(length = 255)
    private String description;

    @Size(min = 7, max = 7, message = "Fill color hex code can only be 7 characters long")
    @Column(name = "fill_color", nullable = false, length = 7)
    private String fillColor = "#000000";

    @Size(min = 7, max = 7, message = "Font color hex code can only be 7 characters long")
    @Column(name = "font_color", nullable = false, length = 7)
    private String fontColor = "#ffffff";

    @Size(max = 255, message = "Icon url can't be longer than 255 characters")
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "is_system", nullable = false)
    private boolean isSystem = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type = CategoryType.EXPENSE;

    protected Category(){}

    public Category(
            Ledger ledger,
            String name,
            String description,
            String fillColor,
            String fontColor,
            String iconUrl,
            Boolean isSystem,
            CategoryType type
    ){
        this.ledger = ledger;
        this.name = name;
        this.description = description;
        this.fillColor = fillColor;
        this.fontColor = fontColor;
        this.iconUrl = iconUrl;
        this.isSystem = isSystem;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
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

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }
}
