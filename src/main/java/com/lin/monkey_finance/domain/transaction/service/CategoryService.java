package com.lin.monkey_finance.domain.transaction.service;

import com.lin.monkey_finance.common.exception.BadRequestException;
import com.lin.monkey_finance.domain.account.dto.CategoryValidationObject;
import com.lin.monkey_finance.domain.ledger.event.LedgerActivityLogEvent;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.transaction.dto.*;
import com.lin.monkey_finance.domain.transaction.mapper.CategoryMapper;
import com.lin.monkey_finance.domain.transaction.mapper.CategorySettingsMapper;
import com.lin.monkey_finance.domain.transaction.model.Category;
import com.lin.monkey_finance.domain.transaction.model.CategorySettings;
import com.lin.monkey_finance.domain.transaction.model.CategorySettingsId;
import com.lin.monkey_finance.domain.transaction.repository.CategoryRepository;
import com.lin.monkey_finance.domain.transaction.repository.CategorySettingsRepository;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lin.monkey_finance.common.exception.IllegalArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper categoryMapper;
    private final CategorySettingsRepository settingsRepository;
    private final CategorySettingsMapper settingsMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    public CategoryService(
            CategoryRepository repository,
            CategoryMapper categoryMapper,
            CategorySettingsRepository settingsRepository,
            CategorySettingsMapper settingsMapper,
            ApplicationEventPublisher eventPublisher,
            EntityManager entityManager
    ){
        this.repository = repository;
        this.categoryMapper = categoryMapper;
        this.settingsRepository = settingsRepository;
        this.settingsMapper = settingsMapper;
        this.eventPublisher = eventPublisher;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public CategoryValidationObject validateAndGet(UUID ledgerId, UUID categoryId, UUID subcategoryId){
        Category category = repository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException("No category with such id"));
        if (!category.isSystem())
            if (category.getLedger() == null || !category.getLedger().getId().equals(ledgerId))
                throw new BadRequestException("This category doesn't belong to this ledger");

        if (subcategoryId != null){
            if (categoryId.equals(subcategoryId))
                throw new IllegalArgumentException("Category and subcategory can't be the same");

            Category subcategory = repository.findById(subcategoryId)
                    .orElseThrow(() -> new BadRequestException("No subcategory with such id"));

            if (subcategory.getParent() == null || !subcategory.getParent().getId().equals(categoryId))
                throw new BadRequestException("This subcategory doesn't belong to this category");

            if (!subcategory.isSystem())
                if (subcategory.getLedger() == null || !subcategory.getLedger().getId().equals(ledgerId))
                    throw new BadRequestException("This subcategory doesn't belong to this ledger");

            return new CategoryValidationObject(category, subcategory);
        }

        return new CategoryValidationObject(category, null);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getSystemCategories(){
        return repository.findAllByIsSystemTrue()
                .stream().map(categoryMapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@ledgerSecurity.isActiveMember(#ledgerId, #userId)")
    public List<CategoryResponseDto> getLedgerCategories(UUID ledgerId, UUID userId){
        return repository.findAllWithSettingsByLedgerId(ledgerId)
                .stream().map(categoryMapper::toResponseDto).toList();
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public CategoryResponseDto editCategory(UUID ledgerId, UUID userId, UUID categoryId, CategoryUpdateDto dto){

        Category category = validateAndGet(ledgerId, categoryId, null).category();
        if (category.isSystem()){
            CategorySettings categorySettings = settingsRepository.findById(new CategorySettingsId(ledgerId, categoryId))
                    .map(existingSettings -> {
                        settingsMapper.updateFromDto(dto, existingSettings);
                        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                                ledgerId,
                                userId,
                                categoryId,
                                LedgerActionType.CATEGORY_EDITED,
                                "Category settings were edited"
                        ));
                        return existingSettings;
                    })
                    .orElseGet(() -> {

                        CategorySettings newSettings = new CategorySettings(
                                category.getLedger(),
                                category,
                                dto.name() != null ? dto.name() : category.getName(),
                                dto.description() != null ? dto.description() : category.getDescription(),
                                dto.fillColor() != null ? dto.fillColor() : category.getFillColor(),
                                dto.fontColor() != null ? dto.fontColor() : category.getFontColor(),
                                dto.iconUrl() != null ? dto.iconUrl() : category.getIconUrl(),
                                Boolean.TRUE.equals(dto.isHidden())
                        );
                        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                                ledgerId,
                                userId,
                                categoryId,
                                LedgerActionType.CATEGORY_SETTINGS_ADDED,
                                "Category settings were added"
                        ));
                        return settingsRepository.save(newSettings);
                    });
            return categoryMapper.toResponseDto(new CategoryWithSettingsDto(category, categorySettings));
        }
        else {
            categoryMapper.updateFromDto(dto, category);
            eventPublisher.publishEvent(new LedgerActivityLogEvent(
                    ledgerId,
                    userId,
                    categoryId,
                    LedgerActionType.CATEGORY_EDITED,
                    "Category \"" + category.getName() + "\" was edited"
            ));
            return categoryMapper.toResponseDto(category);
        }

    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public void deleteCategory(UUID ledgerId, UUID userId, UUID categoryId){
        Category category = validateAndGet(ledgerId, categoryId, null).category();

        if (category.isSystem()) {
            settingsRepository.findById(new CategorySettingsId(ledgerId, categoryId))
                    .ifPresent(settingsRepository::delete);

            eventPublisher.publishEvent(new LedgerActivityLogEvent(
                    ledgerId,
                    userId,
                    categoryId,
                    LedgerActionType.CATEGORY_SETTINGS_DELETED,
                    "Category settings were deleted"
            ));
        }
        else {
            eventPublisher.publishEvent(new LedgerActivityLogEvent(
                    ledgerId,
                    userId,
                    categoryId,
                    LedgerActionType.CATEGORY_DELETED,
                    "Category " + category.getName() + " was deleted"
            ));
            repository.delete(category);
        }
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public CategoryResponseDto createCategory(UUID ledgerId, UUID userId, CategoryCreateDto createDto){
        Category category = new Category(
                entityManager.getReference(Ledger.class, ledgerId),
                null,
                createDto.name(),
                createDto.description(),
                createDto.fillColor() != null ? createDto.fillColor() : "#000000",
                createDto.fontColor() != null ? createDto.fontColor() : "#ffffff",
                createDto.iconUrl(),
                false,
                createDto.type(),
                new ArrayList<>()
        );

        Category savedCategory = repository.saveAndFlush(category);

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                savedCategory.getId(),
                LedgerActionType.CATEGORY_CREATED,
                "Category " + category.getName() + " was created"
        ));

        return categoryMapper.toResponseDto(savedCategory);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public CategoryResponseDto createSubcategory(UUID ledgerId, UUID userId, UUID parentId, SubcategoryCreateDto createDto){
        CategoryValidationObject categoryValidationObject = validateAndGet(ledgerId, parentId, null);
        Category category = categoryValidationObject.category();

        if (category.getParent() != null)
            throw new IllegalArgumentException("Can't create a subcategory of a subcategory");

        Category subcategory = new Category(
                entityManager.getReference(Ledger.class, ledgerId),
                categoryValidationObject.category(),
                createDto.name(),
                createDto.description(),
                createDto.fillColor() != null ? createDto.fillColor() :  category.getFillColor(),
                createDto.fontColor()!= null ? createDto.fontColor() : category.getFontColor(),
                createDto.iconUrl(),
                false,
                categoryValidationObject.category().getType(),
                new ArrayList<>()
        );

        Category savedSubcategory = repository.save(subcategory);

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                savedSubcategory.getId(),
                LedgerActionType.SUBCATEGORY_ADDED,
                "Subcategory " + subcategory.getName() + " was added to the category " + category.getName()
        ));

        return categoryMapper.toResponseDto(savedSubcategory);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public CategoryResponseDto editSubcategory(UUID ledgerId, UUID userId, UUID categoryId, UUID subcategoryId, SubcategoryUpdateDto updateDto ){
        CategoryValidationObject categoryValidationObject = validateAndGet(ledgerId, categoryId, subcategoryId);

        categoryMapper.updateFromDto(updateDto, categoryValidationObject.subcategory());

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                subcategoryId,
                LedgerActionType.SUBCATEGORY_EDITED,
                "Subcategory was edited"
        ));
        return categoryMapper.toResponseDto(categoryValidationObject.subcategory());
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public void deleteSubcategory(UUID ledgerId, UUID userId, UUID categoryId, UUID subcategoryId){
        CategoryValidationObject categoryValidationObject = validateAndGet(ledgerId, categoryId, subcategoryId);

        repository.delete(categoryValidationObject.subcategory());
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                subcategoryId,
                LedgerActionType.SUBCATEGORY_DELETED,
                "Subcategory was deleted"
        ));
    }
}
