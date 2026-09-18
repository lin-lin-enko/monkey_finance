package com.lin.monkey_finance.domain.transaction.service;

import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.service.LedgerActivityLogService;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.transaction.dto.*;
import com.lin.monkey_finance.domain.transaction.mapper.CategoryMapper;
import com.lin.monkey_finance.domain.transaction.mapper.CategorySettingsMapper;
import com.lin.monkey_finance.domain.transaction.model.Category;
import com.lin.monkey_finance.domain.transaction.model.CategorySettings;
import com.lin.monkey_finance.domain.transaction.model.CategorySettingsId;
import com.lin.monkey_finance.domain.transaction.repository.CategoryRepository;
import com.lin.monkey_finance.domain.transaction.repository.CategorySettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategorySettingsRepository settingsRepository;
    private final CategorySettingsMapper settingsMapper;
    private final LedgerMembershipService membershipService;
    private final LedgerActivityLogService activityLogService;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper,
            CategorySettingsRepository settingsRepository,
            CategorySettingsMapper settingsMapper,
            LedgerMembershipService membershipService,
            LedgerActivityLogService activityLogService
    ){
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.settingsRepository = settingsRepository;
        this.settingsMapper = settingsMapper;
        this.membershipService = membershipService;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public Category getReferenceById(UUID categoryId){
        if (categoryRepository.existsById(categoryId))
            return categoryRepository.getReferenceById(categoryId);
        else throw new ResourceNotFoundException("No category/subcategory with such id");
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getSystemCategories(){
        return categoryRepository.findAllByIsSystemTrue()
                .stream().map(categoryMapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getLedgerCategories(UUID ledgerId, UUID userId){
        membershipService.getReferenceById(ledgerId, userId);
        return categoryRepository.findAllWithSettingsByLedgerId(ledgerId)
                .stream().map(categoryMapper::toResponseDto).toList();
    }

    @Transactional
    public CategoryResponseDto editCategory(UUID ledgerId, UUID categoryId, CategoryUpdateDto dto, UUID userId){

        checkPermissions(ledgerId, userId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No category with such id"));

        if (category.getLedger() != null && !category.getLedger().getId().equals(ledgerId))
            throw new IllegalArgumentException("This category doesn't belong to this ledger and is not a custom category");

        if (category.isSystem()){
            CategorySettings categorySettings = settingsRepository.findById(new CategorySettingsId(ledgerId, categoryId))
                    .map(existingSettings -> {
                        settingsMapper.updateFromDto(dto, existingSettings);
                        activityLogService.create(
                                ledgerId,
                                userId,
                                categoryId,
                                LedgerActionType.CATEGORY_EDITED,
                                "Category settings were edited"
                        );
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
                        activityLogService.create(
                                ledgerId,
                                userId,
                                categoryId,
                                LedgerActionType.CATEGORY_SETTINGS_ADDED,
                                "Category settings were added"
                        );
                        return settingsRepository.save(newSettings);
                    });
            return categoryMapper.toResponseDto(new CategoryWithSettingsDto(category, categorySettings));
        }
        else {
            if(category.getLedger() == null || !category.getLedger().getId().equals(ledgerId))
                throw new ResourceNotFoundException("This category is not from this ledger");
            categoryMapper.updateFromDto(dto, category);
            activityLogService.create(
                    ledgerId,
                    userId,
                    categoryId,
                    LedgerActionType.CATEGORY_EDITED,
                    "Category \"" + category.getName() + "\" was edited"
            );
            return categoryMapper.toResponseDto(category);
        }

    }

    @Transactional
    public void deleteCategory(UUID ledgerId, UUID categoryId, UUID userId){
        checkPermissions(ledgerId, userId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No category with such id"));

        if (category.isSystem()) {
            settingsRepository.findById(new CategorySettingsId(ledgerId, categoryId))
                    .ifPresent(settingsRepository::delete);
            activityLogService.create(
                    ledgerId,
                    userId,
                    categoryId,
                    LedgerActionType.CATEGORY_SETTINGS_DELETED,
                    "Category settings were deleted"
            );
        }
        else {
             if (category.getLedger() != null && !category.getLedger().getId().equals(ledgerId))
                 throw new IllegalArgumentException("This category doesn't belong to this ledger and is not a custom category");
            activityLogService.create(
                    ledgerId,
                    userId,
                    categoryId,
                    LedgerActionType.CATEGORY_DELETED,
                    "Category " + category.getName() + " was deleted"
            );
            categoryRepository.delete(category);
        }
    }

    @Transactional
    public CategoryResponseDto createCategory(UUID ledgerId, UUID userId, CategoryCreateDto requestDto){
        checkPermissions(ledgerId, userId);
        LedgerMembership membershipReference = membershipService.getReferenceById(ledgerId, userId);
        Category category = new Category(
                membershipReference.getLedger(),
                null,
                requestDto.name(),
                requestDto.description(),
                requestDto.fillColor() != null ? requestDto.fillColor() : "#000000",
                requestDto.fontColor() != null ? requestDto.fontColor() : "#ffffff",
                requestDto.iconUrl(),
                false,
                requestDto.type(),
                new ArrayList<>()
        );

        Category savedCategory = categoryRepository.saveAndFlush(category);

        activityLogService.create(
                ledgerId,
                userId,
                savedCategory.getId(),
                LedgerActionType.CATEGORY_CREATED,
                "Category " + category.getName() + " was created"
        );

        return categoryMapper.toResponseDto(savedCategory);
    }

    @Transactional
    public CategoryResponseDto createSubcategory(UUID ledgerId, UUID parentId, UUID userId, SubcategoryCreateDto requestDto){
        checkPermissions(ledgerId, userId);
        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("No category with such id"));

        if (parentCategory.getParent() != null)
            throw new IllegalArgumentException("Can't create a subcategory of a subcategory");

        if (!parentCategory.isSystem() && (parentCategory.getLedger() == null || !parentCategory.getLedger().getId().equals(ledgerId)))
            throw new ResourceNotFoundException("This category doesn't belong to this ledger");

        LedgerMembership memberShipReference = membershipService.getReferenceById(ledgerId, userId);
        Category subcategory = new Category(
                memberShipReference.getLedger(),
                parentCategory,
                requestDto.name(),
                requestDto.description(),
                parentCategory.getFillColor(),
                parentCategory.getFontColor(),
                requestDto.iconUrl(),
                false,
                parentCategory.getType(),
                new ArrayList<>()
        );

        settingsRepository.findById(new CategorySettingsId(ledgerId, parentCategory.getId())).ifPresent(
                settings -> {
                    if (settings.getCustomFillColor() != null) subcategory.setFillColor(settings.getCustomFillColor());
                    if(settings.getCustomFontColor() != null) subcategory.setFontColor(settings.getCustomFontColor());
                }
        );

        Category savedSubcategory = categoryRepository.save(subcategory);
        activityLogService.create(
                ledgerId,
                userId,
                savedSubcategory.getId(),
                LedgerActionType.SUBCATEGORY_ADDED,
                "Subcategory " + subcategory.getName() + " was added to the category " + parentCategory.getName()
        );
        return categoryMapper.toResponseDto(savedSubcategory);
    }

    @Transactional
    public CategoryResponseDto editSubcategory(UUID ledgerId, UUID categoryId, UUID subcategoryId, UUID userId, SubcategoryUpdateDto updateDto ){
        checkPermissions(ledgerId, userId);
        Category subcategory = categoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No subcategory with such id"));
        if (subcategory.getParent() == null || !categoryId.equals(subcategory.getParent().getId()))
            throw new ResourceNotFoundException("This subcategory doesn't belong to this category");

        categoryMapper.updateFromDto(updateDto, subcategory);

        activityLogService.create(
                ledgerId,
                userId,
                subcategoryId,
                LedgerActionType.SUBCATEGORY_EDITED,
                "Subcategory was edited"
        );
        return categoryMapper.toResponseDto(subcategory);
    }

    @Transactional
    public void deleteSubcategory(UUID ledgerId, UUID categoryId, UUID subcategoryId, UUID userId){
        checkPermissions(ledgerId, userId);
        Category subcategory = categoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No subcategory with such id"));
        if (subcategory.getParent() == null || !categoryId.equals(subcategory.getParent().getId()))
            throw new ResourceNotFoundException("This subcategory doesn't belong to this category");

        categoryRepository.delete(subcategory);
        activityLogService.create(
                ledgerId,
                userId,
                subcategoryId,
                LedgerActionType.SUBCATEGORY_DELETED,
                "Subcategory was deleted"
        );
    }

    private void checkPermissions(UUID ledgerId, UUID userId){
        LedgerMembershipResponseDto membershipResponseDto = membershipService.getById(ledgerId, userId);

        if (membershipResponseDto.accessType() != AccessType.ADMIN && membershipResponseDto.accessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can perform this action");

        if (membershipResponseDto.status() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Only active users can perform this action");
    }
}
