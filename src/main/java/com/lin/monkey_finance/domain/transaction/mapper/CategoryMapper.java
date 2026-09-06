package com.lin.monkey_finance.domain.transaction.mapper;

import com.lin.monkey_finance.domain.transaction.dto.CategoryUpdateDto;
import com.lin.monkey_finance.domain.transaction.dto.CategoryResponseDto;
import com.lin.monkey_finance.domain.transaction.dto.CategoryWithSettingsDto;
import com.lin.monkey_finance.domain.transaction.model.Category;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "ledgerId", source = "ledger.id")
    @Mapping(target = "isSystem", source = "system")
    CategoryResponseDto toResponseDto(Category category);

    @Mapping(target = "id", source = "dto.category.id")
    @Mapping(target = "ledgerId", expression = "java(resolveLedgerId(dto))")
    @Mapping(target = "name", expression = "java(resolveName(dto))")
    @Mapping(target = "description", expression = "java(resolveDescription(dto))")
    @Mapping(target = "fillColor", expression = "java(resolveFillColor(dto))")
    @Mapping(target = "fontColor", expression = "java(resolveFontColor(dto))")
    @Mapping(target = "iconUrl", expression = "java(resolveIconUrl(dto))")
    @Mapping(target = "isSystem", source = "dto.category.system")
    @Mapping(target = "type", source = "dto.category.type")
    CategoryResponseDto toResponseDto(CategoryWithSettingsDto dto);

    default UUID resolveLedgerId(CategoryWithSettingsDto dto){
        var category = dto.category();
        var categorySettings = dto.categorySettings();

        if (categorySettings != null && categorySettings.getLedger() != null) return categorySettings.getLedger().getId();
        else if (category.getLedger() != null) return category.getLedger().getId();
        else return null;
    }

    default String resolveName(CategoryWithSettingsDto dto){
        var categorySettings = dto.categorySettings();

        if (categorySettings != null && categorySettings.getCustomName() != null) return categorySettings.getCustomName();
        else return dto.category().getName();
    }

    default String resolveDescription(CategoryWithSettingsDto dto){
        var categorySettings = dto.categorySettings();

        if (categorySettings != null && categorySettings.getCustomDescription() != null) return categorySettings.getCustomDescription();
        else return dto.category().getDescription();
    }

    default String resolveFillColor(CategoryWithSettingsDto dto){
        var categorySettings = dto.categorySettings();

        if (categorySettings != null && categorySettings.getCustomFillColor() != null) return categorySettings.getCustomFillColor();
        else return dto.category().getFillColor();
    }

    default String resolveFontColor(CategoryWithSettingsDto dto){
        var categorySettings = dto.categorySettings();

        if (categorySettings != null && categorySettings.getCustomFontColor() != null) return categorySettings.getCustomFontColor();
        else return dto.category().getFontColor();
    }

    default String resolveIconUrl(CategoryWithSettingsDto dto){
        var categorySettings = dto.categorySettings();

        if (categorySettings != null && categorySettings.getCustomIconUrl() != null) return categorySettings.getCustomIconUrl();
        else return dto.category().getIconUrl();
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CategoryUpdateDto dto, @MappingTarget Category category);
}
