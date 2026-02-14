package com.lin.monkey.repository;

import com.lin.monkey.model.Ledger;
import com.lin.monkey.model.UsersLedgers;
import com.lin.monkey.model.UsersLedgersId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersLedgersRepository extends JpaRepository<UsersLedgers, UsersLedgersId> {

    boolean existsByUserIdAndLedgerId(UUID userId, UUID ledgerId);

    List<UsersLedgers> findByUserId(UUID userId);

    List<UsersLedgers> findByLedgerId(UUID ledgerId);

    @Query("SELECT CASE WHEN COUNT (ul) > 0 THEN true ELSE false END " +
            "FROM UsersLedgers ul " +
            "WHERE ul.userId = :userId " +
            "AND ul.ledgerId = :ledgerId " +
            "AND ul.role = :role")
    boolean existsByUserIdAndLedgerIdAndRole(
            @Param("userId") UUID userId,
            @Param("ledgerId") UUID ledgerId,
            @Param("role") String role

    );

    @Query("SELECT ul.role " +
            "FROM UsersLedgers ul " +
            "WHERE ul.userId = :userId " +
            "AND ul.ledgerId = :ledgerId")
    Optional<String> findUserRoleInLedger(
            @Param("userId") UUID userId,
            @Param("ledgerId") UUID ledgerId
    );

    @Query("SELECT ul " +
            "FROM UsersLedgers ul " +
            "WHERE ul.ledgerId = :ledgerId")
    List<UsersLedgers> findAllUsersAndRolesInLedger(@Param("ledgerId") UUID ledgerId);
}