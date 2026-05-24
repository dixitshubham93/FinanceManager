package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.CategoryRequest;
import com.syfe.financemanager.dto.response.CategoryListResponse;
import com.syfe.financemanager.dto.response.CategoryResponse;
import com.syfe.financemanager.dto.response.MessageResponse;
import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.exception.BadRequestException;
import com.syfe.financemanager.exception.ConflictException;
import com.syfe.financemanager.exception.ForbiddenException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.repository.CategoryRepository;
import com.syfe.financemanager.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public CategoryListResponse getAllCategories() {
        Long userId = securityUtils.getCurrentUserId();
        List<CategoryResponse> categories = categoryRepository
                .findAllAccessibleByUserId(userId)
                .stream()
                .map(CategoryResponse::from)
                .toList();
        return new CategoryListResponse(categories);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = securityUtils.getCurrentUser();

        if (categoryRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new ConflictException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .isCustom(true)
                .user(user)
                .isDeleted(false)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Custom category created: '{}' for user {}", saved.getName(), user.getUsername());
        return CategoryResponse.from(saved);
    }

    @Transactional
    public MessageResponse deleteCategory(String categoryName) {
        Long userId = securityUtils.getCurrentUserId();

        Category category = categoryRepository
                .findAccessibleByNameAndUserId(categoryName, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryName));

        if (!category.isCustom()) {
            throw new ForbiddenException("Default system categories cannot be deleted");
        }

        if (categoryRepository.hasActiveTransactions(category.getId())) {
            throw new BadRequestException(
                    "Category '" + categoryName + "' cannot be deleted as it is referenced by existing transactions");
        }

        category.setDeleted(true);
        categoryRepository.save(category);
        log.info("Category soft-deleted: '{}' for user {}", categoryName, userId);

        return new MessageResponse("Category deleted successfully");
    }
}
