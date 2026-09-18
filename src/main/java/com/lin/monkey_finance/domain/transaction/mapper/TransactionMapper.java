package com.lin.monkey_finance.domain.transaction.mapper;

import com.lin.monkey_finance.domain.transaction.dto.TransactionResponseDto;
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

}
