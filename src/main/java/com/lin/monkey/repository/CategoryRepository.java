package com.lin.monkey.repository;

import com.lin.monkey.model.Category;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @NonNull Optional<Category> findById(@NonNull UUID id);

    boolean existsById(UUID id);
}