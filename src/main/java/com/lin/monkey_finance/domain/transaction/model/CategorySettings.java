package com.lin.monkey_finance.domain.transaction.model;

import com.lin.monkey_finance.domain.ledger.model.Ledger;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "category_settings", schema = "dev")
public class CategorySettings {

    @EmbeddedId
    private CategorySettingsId id;

    @MapsId("ledgerId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_id")
    private Ledger ledger;

    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Size(min = 2, max = 50, message = "Custom name can only be 2 to 50 characters long")
    @Column(name = "custom_name", length = 50)
    private String customName;

    @Size(max = 255, message = "Custom description can't be longer than 255 characters")
    @Column(name = "custom_description", length = 255)
    private String customDescription;

    @Size(min = 7, max = 7, message = "Custom fill color hex code can only be 7 characters long")
    @Column(name = "custom_fill_color", nullable = false, length = 7)
    private String customFillColor = "#000000";

    @Size(min = 7, max = 7, message = "Custom font color hex code can only be 7 characters long")
    @Column(name = "custom_font_color", nullable = false, length = 7)
    private String customFontColor = "#ffffff";

    @Size(max = 255, message = "Custom icon url can't be longer than 255 characters")
    @Column(name = "custom_icon_url", length = 255)
    private String customIconUrl;

    @Column(name = "is_hidden")
    private boolean isHidden = false;

    protected CategorySettings(){}

    public CategorySettings(
            Ledger ledger,
            Category category,
            String customName,
            String customDescription,
            String customFillColor,
            String customFontColor,
            String customIconUrl,
            Boolean isHidden
    ){
        this.id = new CategorySettingsId(ledger.getId(), category.getId());
        this.ledger = ledger;
        this.category = category;
        this.customName = customName;
        this.customDescription = customDescription;
        this.customFillColor = customFillColor;
        this.customFontColor = customFontColor;
        this.customIconUrl = customIconUrl;
        this.isHidden = isHidden;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCustomDescription() {
        return customDescription;
    }

    public void setCustomDescription(String customDescription) {
        this.customDescription = customDescription;
    }

    public String getCustomFillColor() {
        return customFillColor;
    }

    public void setCustomFillColor(String customFillColor) {
        this.customFillColor = customFillColor;
    }

    public String getCustomFontColor() {
        return customFontColor;
    }

    public void setCustomFontColor(String customFontColor) {
        this.customFontColor = customFontColor;
    }

    public String getCustomIconUrl() {
        return customIconUrl;
    }

    public void setCustomIconUrl(String customIconUrl) {
        this.customIconUrl = customIconUrl;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }
}
