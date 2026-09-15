package com.lin.monkey_finance.domain.savings.repository;

import com.lin.monkey_finance.domain.savings.model.Savings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavingsRepository extends JpaRepository<Savings, UUID>{
    List<Savings> findAllByAccountId(UUID accountId);
}
