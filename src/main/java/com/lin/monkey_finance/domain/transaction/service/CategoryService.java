package com.lin.monkey_finance.domain.transaction.service;

import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import com.lin.monkey_finance.domain.transaction.dto.CategoryRequestDto;
import com.lin.monkey_finance.domain.transaction.dto.CategoryResponseDto;
import com.lin.monkey_finance.domain.transaction.dto.CategoryWithSettingsDto;
import com.lin.monkey_finance.domain.transaction.mapper.CategoryMapper;
import com.lin.monkey_finance.domain.transaction.mapper.CategorySettingsMapper;
import com.lin.monkey_finance.domain.transaction.model.Category;
import com.lin.monkey_finance.domain.transaction.model.CategorySettings;
import com.lin.monkey_finance.domain.transaction.model.CategorySettingsId;
import com.lin.monkey_finance.domain.transaction.repository.CategoryRepository;
import com.lin.monkey_finance.domain.transaction.repository.CategorySettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final LedgerRepository ledgerRepository;
    private final CategoryMapper categoryMapper;
    private final CategorySettingsRepository settingsRepository;
    private final CategorySettingsMapper settingsMapper;

    public CategoryService(
            CategoryRepository categoryRepository,
            LedgerRepository ledgerRepository,
            CategoryMapper categoryMapper,
            CategorySettingsRepository settingsRepository,
            CategorySettingsMapper settingsMapper
    ){
        this.categoryRepository = categoryRepository;
        this.ledgerRepository = ledgerRepository;
        this.categoryMapper = categoryMapper;
        this.settingsRepository = settingsRepository;
        this.settingsMapper = settingsMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getSystemCategories(){
        return categoryRepository.findAllByIsSystemTrue()
                .stream().map(categoryMapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getLedgerCategories(UUID ledgerId){
        if(!ledgerRepository.existsById(ledgerId)){
            throw new ResourceNotFoundException("No ledger with such id");
        }

        return categoryRepository.findAllWithSettingsByLedgerId(ledgerId)
                .stream().map(categoryMapper::toResponseDto).toList();
    }

    @Transactional
    public CategoryResponseDto modifyCategory(UUID ledgerId, UUID categoryId, CategoryRequestDto dto){
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("No ledger with such id"));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No category with such id"));

        if (category.isSystem()){
            Optional<CategorySettings> settingsOptional = settingsRepository.findById(new CategorySettingsId(ledgerId, categoryId));

            if (settingsOptional.isPresent()) {
                CategorySettings categorySettings = settingsOptional.get();
                settingsMapper.updateFromDto(dto, categorySettings);
                CategoryWithSettingsDto categoryWithSettingsDto = new CategoryWithSettingsDto(category, categorySettings);
                return categoryMapper.toResponseDto(categoryWithSettingsDto);
            }
            else {
                CategorySettings settings = new CategorySettings(
                        ledger,
                        category,
                        dto.name() != null ? dto.name() : category.getName(),
                        dto.description() != null ? dto.description() : category.getDescription(),
                        dto.fillColor() != null ? dto.fillColor() : category.getFillColor(),
                        dto.fontColor() != null ? dto.fontColor() : category.getFontColor(),
                        dto.iconUrl() != null ? dto.iconUrl() : category.getIconUrl(),
                        Boolean.TRUE.equals(dto.isHidden())
                );
                CategorySettings savedSettings = settingsRepository.save(settings);
                CategoryWithSettingsDto categoryWithSettingsDto = new CategoryWithSettingsDto(category, savedSettings);
                return categoryMapper.toResponseDto(categoryWithSettingsDto);
            }
        }
        else {
            if(category.getLedger() == null || !category.getLedger().getId().equals(ledgerId))
                throw new ResourceNotFoundException("This category is not from this ledger");
            categoryMapper.updateFromDto(dto, category);
            return categoryMapper.toResponseDto(category);
        }

    }

    @Transactional
    public void deleteCategory(UUID ledgerId, UUID categoryId){

    }
}
