package com.lin.monkey_finance.domain.transaction.mapper;

import com.lin.monkey_finance.domain.transaction.dto.TransactionCreateDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionResponseDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionUpdateDto;
import com.lin.monkey_finance.domain.transaction.model.Transaction;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "subcategory.id", target = "subcategoryId")
    @Mapping(source = "ledger.id", target = "ledgerId")
    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "savingsPot.id", target = "savingsPotId")
    TransactionResponseDto toResponseDto(Transaction transaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subcategory", ignore = true)
    @Mapping(target = "ledger", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "savingsPot", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Transaction toEntity(TransactionCreateDto createDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subcategory", ignore = true)
    @Mapping(target = "ledger", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "savingsPot", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(TransactionUpdateDto updateDto, @MappingTarget Transaction transaction);
}
