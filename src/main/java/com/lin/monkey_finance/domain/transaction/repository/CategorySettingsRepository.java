package com.lin.monkey_finance.domain.transaction.repository;

import com.lin.monkey_finance.domain.transaction.model.Category;
import com.lin.monkey_finance.domain.transaction.model.CategorySettings;
import com.lin.monkey_finance.domain.transaction.model.CategorySettingsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategorySettingsRepository extends JpaRepository<CategorySettings, CategorySettingsId> {
    List<Category> findAllByLedgerId(UUID ledgerId);
}
