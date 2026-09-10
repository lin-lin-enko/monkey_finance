package com.lin.monkey_finance.domain.transaction.controller;

import com.lin.monkey_finance.domain.transaction.dto.*;
import com.lin.monkey_finance.domain.transaction.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(
        CategoryService categoryService
    ){
        this.categoryService = categoryService;
    }

    @GetMapping("/categories/system")
    public ResponseEntity<List<CategoryResponseDto>> getSystemCategories(
    ){
        List<CategoryResponseDto> categories = categoryService.getSystemCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/ledgers/{ledgerId}/categories")
    public ResponseEntity<List<CategoryResponseDto>> getLedgerCategories(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        List<CategoryResponseDto> categories = categoryService.getLedgerCategories(ledgerId, userId);
        return ResponseEntity.ok(categories);
    }

    @PatchMapping("/ledgers/{ledgerId}/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDto> editCategory(
            @PathVariable UUID ledgerId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateDto requestDto,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        CategoryResponseDto responseDto = categoryService.editCategory(ledgerId, categoryId, requestDto, userId);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/ledgers/{ledgerId}/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID ledgerId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        categoryService.deleteCategory(ledgerId, categoryId, userId);
        return ResponseEntity.status(204).build();
    }

    @PostMapping("/ledgers/{ledgerId}/categories")
    public ResponseEntity<CategoryResponseDto> createCategory(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CategoryCreateDto requestDto
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        CategoryResponseDto responseDto = categoryService.createCategory(ledgerId, userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/ledgers/{ledgerId}/categories/{categoryId}/subcategories")
    public ResponseEntity<CategoryResponseDto> createSubcategory(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SubcategoryCreateDto requestDto,
            @PathVariable UUID categoryId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CategoryResponseDto responseDto = categoryService.createSubcategory(ledgerId, categoryId, userId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PatchMapping("/ledgers/{ledgerId}/categories/{categoryId}/subcategories/{subcategoryId}")
    public ResponseEntity<CategoryResponseDto> editSubcategory(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SubcategoryUpdateDto requestDto,
            @PathVariable UUID categoryId,
            @PathVariable UUID subcategoryId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CategoryResponseDto responseDto = categoryService.editSubcategory(ledgerId, categoryId, subcategoryId, userId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/ledgers/{ledgerId}/categories/{categoryId}/subcategories/{subcategoryId}")
    public ResponseEntity<Void> deleteSubcategory(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID categoryId,
            @PathVariable UUID subcategoryId
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        categoryService.deleteSubcategory(ledgerId, categoryId, subcategoryId, userId);
        return ResponseEntity.status(204).build();
    }
}
