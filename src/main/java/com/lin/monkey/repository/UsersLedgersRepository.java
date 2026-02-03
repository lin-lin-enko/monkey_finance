package com.lin.monkey.repository;

import com.lin.monkey.model.Ledger;
import com.lin.monkey.model.UsersLedgers;
import com.lin.monkey.model.UsersLedgersId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UsersLedgersRepository extends JpaRepository<UsersLedgers, UsersLedgersId> {

    boolean existsByUserIdAndLedgerId(UUID userId, UUID ledgerId);

    List<UsersLedgers> findByUserId(UUID userId);

    List<UsersLedgers> findByLedgerId(UUID ledgerId);
}