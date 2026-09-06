package com.lin.monkey_finance.domain.transaction.service;

import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import com.lin.monkey_finance.domain.ledger.model.LedgerMemberId;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
import com.lin.monkey_finance.domain.transaction.dto.CategoryCreateDto;
import com.lin.monkey_finance.domain.transaction.dto.CategoryUpdateDto;
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
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final CategorySettingsRepository settingsRepository;
    private final CategorySettingsMapper settingsMapper;
    private final LedgerMemberRepository memberRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            CategoryMapper categoryMapper,
            CategorySettingsRepository settingsRepository,
            CategorySettingsMapper settingsMapper,
            LedgerMemberRepository memberRepository
    ){
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.settingsRepository = settingsRepository;
        this.settingsMapper = settingsMapper;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getSystemCategories(){
        return categoryRepository.findAllByIsSystemTrue()
                .stream().map(categoryMapper::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getLedgerCategories(UUID ledgerId, UUID userId){
        if(!memberRepository.existsById(new LedgerMemberId(ledgerId, userId)))
            throw new ResourceNotFoundException("This user is not a member of this ledger or the ledger/user don't exist");

        return categoryRepository.findAllWithSettingsByLedgerId(ledgerId)
                .stream().map(categoryMapper::toResponseDto).toList();
    }

    @Transactional
    public CategoryResponseDto edit(UUID ledgerId, UUID categoryId, CategoryUpdateDto dto, UUID userId){
        LedgerMember currentMember = memberRepository.findById(new LedgerMemberId(ledgerId, userId))
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
                        return settingsRepository.save(newSettings);
                    });
            return categoryMapper.toResponseDto(new CategoryWithSettingsDto(category, categorySettings));
        }
        else {
            if(category.getLedger() == null || !category.getLedger().getId().equals(ledgerId))
                throw new ResourceNotFoundException("This category is not from this ledger");
            categoryMapper.updateFromDto(dto, category);
            return categoryMapper.toResponseDto(category);
        }

    }

    @Transactional
    public void delete(UUID ledgerId, UUID categoryId, UUID userId){
        LedgerMember currentMember = memberRepository.findById(new LedgerMemberId(ledgerId, userId))
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
        }
        else {
             if (category.getLedger() != null && !category.getLedger().getId().equals(currentMember.getLedger().getId()))
                 throw new IllegalArgumentException("This category doesn't belong to this ledger and is not a custom category");
             categoryRepository.delete(category);
        }
    }

    @Transactional
    public CategoryResponseDto create(UUID ledgerId, UUID userId, CategoryCreateDto requestDto){
        LedgerMember currentMember = memberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("This user is not a member of this ledger or the ledger/user don't exist"));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can add new categories to the ledger");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("User with status " + currentMember.getStatus() + " cannot create new categories");

        Category category = new Category(
                currentMember.getLedger(),
                requestDto.name(),
                requestDto.description(),
                requestDto.fillColor() != null ? requestDto.fillColor() : "#000000",
                requestDto.fontColor() != null ? requestDto.fontColor() : "#ffffff",
                requestDto.iconUrl(),
                false,
                requestDto.type()
        );

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(savedCategory);
    }
}
