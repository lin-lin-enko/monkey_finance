package com.lin.monkey_finance.domain.transaction.controller;

import com.lin.monkey_finance.domain.transaction.dto.CategoryRequestDto;
import com.lin.monkey_finance.domain.transaction.service.CategoryService;
import com.lin.monkey_finance.domain.transaction.dto.CategoryResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<CategoryResponseDto>> getSystemCategories(){
        List<CategoryResponseDto> categories = categoryService.getSystemCategories();
        return ResponseEntity.ok(categories);
    }

    //remake into getLedgerCategories
    @GetMapping("/ledgers/{ledgerId}/categories")
    public ResponseEntity<List<CategoryResponseDto>> getLedgerCategories(
            @PathVariable UUID ledgerId
            ){

        List<CategoryResponseDto> categories = categoryService.getLedgerCategories(ledgerId);
        return ResponseEntity.ok(categories);
    }

    @PatchMapping("/ledgers/{ledgerId}/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDto> modifyCategory(
            @PathVariable UUID ledgerId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryRequestDto requestDto
    ){

        CategoryResponseDto responseDto = categoryService.modifyCategory(ledgerId, categoryId, requestDto);
        return ResponseEntity.ok(responseDto);
    }
}
