package com.lin.monkey_finance.domain.transaction.repository;

import com.lin.monkey_finance.domain.transaction.dto.CategoryWithSettingsDto;
import com.lin.monkey_finance.domain.transaction.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByLedgerId(UUID ledgerId);
    List<Category> findAllByIsSystemTrue();

    @Query("""
        SELECT new com.lin.monkey_finance.domain.transaction.dto.CategoryWithSettingsDto(cat, cat_set)
        FROM Category cat
        LEFT JOIN CategorySettings cat_set ON cat_set.category = cat 
        AND cat_set.ledger.id = :ledgerId
        AND (cat_set.isHidden = false)
        WHERE (cat.ledger.id = :ledgerId OR cat.isSystem = true)
""")
        List<CategoryWithSettingsDto> findAllWithSettingsByLedgerId(@Param("ledgerId") UUID ledgerId);
}
