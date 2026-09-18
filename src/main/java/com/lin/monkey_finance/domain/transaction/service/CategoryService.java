package com.lin.monkey_finance.domain.transaction.service;

import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerActivityLogRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMembershipRepository;
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
    private final LedgerMembershipRepository memberRepository;
    private final LedgerActivityLogRepository activityLogRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper,
            CategorySettingsRepository settingsRepository,
            CategorySettingsMapper settingsMapper,
            LedgerMembershipRepository memberRepository,
            LedgerActivityLogRepository activityLogRepository
    ){
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.settingsRepository = settingsRepository;
        this.settingsMapper = settingsMapper;
        this.memberRepository = memberRepository;
        this.activityLogRepository = activityLogRepository;
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
        if(!memberRepository.existsById(new LedgerMembershipId(ledgerId, userId)))
            throw new ResourceNotFoundException("This user is not a member of this ledger or the ledger/user don't exist");

        return categoryRepository.findAllWithSettingsByLedgerId(ledgerId)
                .stream().map(categoryMapper::toResponseDto).toList();
    }

    @Transactional
    public CategoryResponseDto editCategory(UUID ledgerId, UUID categoryId, CategoryUpdateDto dto, UUID userId){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("This user is not a member of this ledger or the ledger/user don't exist"));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can modify the categories of the ledger");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("User with status " + currentMember.getStatus() + " cannot modify categories");


        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No category with such id"));

        if (category.getLedger() != null && !category.getLedger().getId().equals(currentMember.getLedger().getId()))
            throw new IllegalArgumentException("This category doesn't belong to this ledger and is not a custom category");

        if (category.isSystem()){
            CategorySettings categorySettings = settingsRepository.findById(new CategorySettingsId(ledgerId, categoryId))
                    .map(existingSettings -> {
                        settingsMapper.updateFromDto(dto, existingSettings);
                        LedgerActivityLog activityLog = new LedgerActivityLog(
                                currentMember.getLedger(),
                                currentMember.getUser(),
                                categoryId,
                                LedgerActionType.CATEGORY_EDITED,
                                "Category settings were edited"
                        );
                        activityLogRepository.save(activityLog);
                        return existingSettings;
                    })
                    .orElseGet(() -> {
                        CategorySettings newSettings = new CategorySettings(
                                currentMember.getLedger(),
                                category,
                                dto.name() != null ? dto.name() : category.getName(),
                                dto.description() != null ? dto.description() : category.getDescription(),
                                dto.fillColor() != null ? dto.fillColor() : category.getFillColor(),
                                dto.fontColor() != null ? dto.fontColor() : category.getFontColor(),
                                dto.iconUrl() != null ? dto.iconUrl() : category.getIconUrl(),
                                Boolean.TRUE.equals(dto.isHidden())
                        );
                        LedgerActivityLog activityLog = new LedgerActivityLog(
                                currentMember.getLedger(),
                                currentMember.getUser(),
                                categoryId,
                                LedgerActionType.CATEGORY_SETTINGS_ADDED,
                                "Category settings were added"
                        );
                        activityLogRepository.save(activityLog);
                        return settingsRepository.save(newSettings);
                    });
            return categoryMapper.toResponseDto(new CategoryWithSettingsDto(category, categorySettings));
        }
        else {
            if(category.getLedger() == null || !category.getLedger().getId().equals(ledgerId))
                throw new ResourceNotFoundException("This category is not from this ledger");
            categoryMapper.updateFromDto(dto, category);
            LedgerActivityLog activityLog = new LedgerActivityLog(
                    currentMember.getLedger(),
                    currentMember.getUser(),
                    categoryId,
                    LedgerActionType.CATEGORY_EDITED,
                    "Category \"" + category.getName() + "\" was edited"
            );
            activityLogRepository.save(activityLog);
            return categoryMapper.toResponseDto(category);
        }

    }

    @Transactional
    public void deleteCategory(UUID ledgerId, UUID categoryId, UUID userId){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("This user is not a member of this ledger or the ledger/user don't exist"));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can delete the categories of the ledger");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("User with status " + currentMember.getStatus() + " cannot delete categories");

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No category with such id"));

        if (category.isSystem()) {
            settingsRepository.findById(new CategorySettingsId(ledgerId, categoryId))
                    .ifPresent(settingsRepository::delete);
            LedgerActivityLog activityLog = new LedgerActivityLog(
                    currentMember.getLedger(),
                    currentMember.getUser(),
                    categoryId,
                    LedgerActionType.CATEGORY_SETTINGS_DELETED,
                    "Category settings were deleted"
            );
            activityLogRepository.save(activityLog);
        }
        else {
             if (category.getLedger() != null && !category.getLedger().getId().equals(currentMember.getLedger().getId()))
                 throw new IllegalArgumentException("This category doesn't belong to this ledger and is not a custom category");
            LedgerActivityLog activityLog = new LedgerActivityLog(
                    currentMember.getLedger(),
                    currentMember.getUser(),
                    categoryId,
                    LedgerActionType.CATEGORY_DELETED,
                    "Category " + category.getName() + " was deleted"
            );
            activityLogRepository.save(activityLog);
            categoryRepository.delete(category);
        }
    }

    @Transactional
    public CategoryResponseDto createCategory(UUID ledgerId, UUID userId, CategoryCreateDto requestDto){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("This user is not a member of this ledger or the ledger/user don't exist"));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can add new categories to the ledger");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("User with status " + currentMember.getStatus() + " cannot create new categories");

        Category category = new Category(
                currentMember.getLedger(),
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

        LedgerActivityLog activityLog = new LedgerActivityLog(
                currentMember.getLedger(),
                currentMember.getUser(),
                savedCategory.getId(),
                LedgerActionType.CATEGORY_CREATED,
                "Category " + category.getName() + " was created"
        );
        activityLogRepository.save(activityLog);

        return categoryMapper.toResponseDto(savedCategory);
    }

    @Transactional
    public CategoryResponseDto createSubcategory(UUID ledgerId, UUID parentId, UUID userId, SubcategoryCreateDto requestDto){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("This user is not a member of this ledger or the ledger/user don't exist"));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can add new subcategories to the ledger");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("User with status " + currentMember.getStatus() + " cannot create new subcategories");

        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("No category with such id"));

        if (parentCategory.getParent() != null)
            throw new IllegalArgumentException("Can't create a subcategory of a subcategory");

        if (!parentCategory.isSystem() && (parentCategory.getLedger() == null || !parentCategory.getLedger().getId().equals(ledgerId)))
            throw new ResourceNotFoundException("This category doesn't belong to this ledger");

        Category subcategory = new Category(
                currentMember.getLedger(),
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

        settingsRepository.findById(new CategorySettingsId(currentMember.getLedger().getId(), parentCategory.getId())).ifPresent(
                settings -> {
                    if (settings.getCustomFillColor() != null) subcategory.setFillColor(settings.getCustomFillColor());
                    if(settings.getCustomFontColor() != null) subcategory.setFontColor(settings.getCustomFontColor());
                }
        );

        Category savedSubcategory = categoryRepository.save(subcategory);
        LedgerActivityLog activityLog = new LedgerActivityLog(
                currentMember.getLedger(),
                currentMember.getUser(),
                parentCategory.getId(),
                LedgerActionType.SUBCATEGORY_ADDED,
                "Subcategory " + subcategory.getName() + " was added to the category " + parentCategory.getName()
        );
        activityLogRepository.save(activityLog);
        return categoryMapper.toResponseDto(savedSubcategory);
    }

    @Transactional
    public CategoryResponseDto editSubcategory(UUID ledgerId, UUID categoryId, UUID subcategoryId, UUID userId, SubcategoryUpdateDto updateDto ){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("This user is not a member of this ledger or the ledger/user don't exist"));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can edit subcategories of the ledger");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("User with status " + currentMember.getStatus() + " cannot edit subcategories");

        Category subcategory = categoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No subcategory with such id"));
        if (!categoryId.equals(subcategory.getParent().getId())) throw new InsufficientPermissionsException("This subcategory doesn't belong to this category");

        categoryMapper.updateFromDto(updateDto, subcategory);

        LedgerActivityLog activityLog = new LedgerActivityLog(
                currentMember.getLedger(),
                currentMember.getUser(),
                subcategoryId,
                LedgerActionType.SUBCATEGORY_EDITED,
                "Subcategory was edited"
        );

        activityLogRepository.save(activityLog);
        return categoryMapper.toResponseDto(subcategory);
    }

    public void deleteSubcategory(UUID ledgerId, UUID categoryId, UUID subcategoryId, UUID userId){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("This user is not a member of this ledger or the ledger/user don't exist"));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can edit subcategories of the ledger");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("User with status " + currentMember.getStatus() + " cannot edit subcategories");

        Category subcategory = categoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("No subcategory with such id"));
        if (!categoryId.equals(subcategory.getParent().getId())) throw new InsufficientPermissionsException("This subcategory doesn't belong to this category");

        categoryRepository.delete(subcategory);
        LedgerActivityLog activityLog = new LedgerActivityLog(
                currentMember.getLedger(),
                currentMember.getUser(),
                subcategoryId,
                LedgerActionType.SUBCATEGORY_DELETED,
                "Subcategory was deleted"
        );
        activityLogRepository.save(activityLog);
    }
}
