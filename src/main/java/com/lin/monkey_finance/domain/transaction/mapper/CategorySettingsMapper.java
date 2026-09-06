package com.lin.monkey_finance.domain.transaction.mapper;

import com.lin.monkey_finance.domain.transaction.dto.CategoryRequestDto;
import com.lin.monkey_finance.domain.transaction.model.CategorySettings;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategorySettingsMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "ledger", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "customName", source = "dto.name")
    @Mapping(target = "customDescription", source = "dto.description")
    @Mapping(target = "customFillColor", source = "dto.fillColor")
    @Mapping(target = "customFontColor", source = "dto.fontColor")
    @Mapping(target = "customIconUrl", source = "dto.iconUrl")
    @Mapping(target = "hidden", source = "dto.isHidden")
    void updateFromDto(CategoryRequestDto dto, @MappingTarget CategorySettings settings);
}
