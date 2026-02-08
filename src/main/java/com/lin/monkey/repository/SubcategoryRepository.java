package com.lin.monkey.repository;

import com.lin.monkey.model.Subcategory;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubcategoryRepository extends JpaRepository<Subcategory, UUID> {

    @NonNull Optional<Subcategory> findById(@NonNull UUID id);

    boolean existsById(UUID id);
}