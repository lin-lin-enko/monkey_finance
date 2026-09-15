package com.lin.monkey_finance.domain.savings.repository;

import com.lin.monkey_finance.domain.savings.model.SavingsPot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavingsPotRepository extends JpaRepository<SavingsPot, UUID>{
    List<SavingsPot> findAllByAccountId(UUID accountId);
}
