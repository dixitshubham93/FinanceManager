package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.CategoryRequest;
import com.syfe.financemanager.dto.response.CategoryListResponse;
import com.syfe.financemanager.dto.response.CategoryResponse;
import com.syfe.financemanager.dto.response.MessageResponse;
import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.enums.TransactionType;
import com.syfe.financemanager.exception.BadRequestException;
import com.syfe.financemanager.exception.ConflictException;
import com.syfe.financemanager.exception.ForbiddenException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.repository.CategoryRepository;
import com.syfe.financemanager.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks private CategoryService categoryService;

    private User user;
    private Category systemCategory;
    private Category customCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@example.com").build();
        systemCategory = Category.builder().id(1L).name("Salary").type(TransactionType.INCOME)
                .isCustom(false).user(null).isDeleted(false).build();
        customCategory = Category.builder().id(2L).name("MyBusiness").type(TransactionType.INCOME)
                .isCustom(true).user(user).isDeleted(false).build();
    }

    @Test
    @DisplayName("GetAll: returns both system and custom categories")
    void getAllCategories_returnsBoth() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findAllAccessibleByUserId(1L))
                .thenReturn(List.of(systemCategory, customCategory));

        CategoryListResponse result = categoryService.getAllCategories();

        assertThat(result.getCategories()).hasSize(2);
        assertThat(result.getCategories().get(0).getName()).isEqualTo("Salary");
        assertThat(result.getCategories().get(0).isCustom()).isFalse();
        assertThat(result.getCategories().get(1).getName()).isEqualTo("MyBusiness");
        assertThat(result.getCategories().get(1).isCustom()).isTrue();
    }

    @Test
    @DisplayName("Create: successfully creates a custom category")
    void createCategory_success() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(categoryRepository.existsByNameAndUserId("NewCat", 1L)).thenReturn(false);

        Category saved = Category.builder().id(3L).name("NewCat")
                .type(TransactionType.EXPENSE).isCustom(true).user(user).isDeleted(false).build();
        when(categoryRepository.save(any())).thenReturn(saved);

        CategoryRequest request = new CategoryRequest();
        request.setName("NewCat");
        request.setType(TransactionType.EXPENSE);

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result.getName()).isEqualTo("NewCat");
        assertThat(result.isCustom()).isTrue();
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
    }

    @Test
    @DisplayName("Create: throws ConflictException when name already exists for user")
    void createCategory_duplicateName_throwsConflict() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(categoryRepository.existsByNameAndUserId("MyBusiness", 1L)).thenReturn(true);

        CategoryRequest request = new CategoryRequest();
        request.setName("MyBusiness");
        request.setType(TransactionType.INCOME);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(ConflictException.class);

        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete: successfully soft-deletes a custom category")
    void deleteCategory_success() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findAccessibleByNameAndUserId("MyBusiness", 1L))
                .thenReturn(Optional.of(customCategory));
        when(categoryRepository.hasActiveTransactions(2L)).thenReturn(false);

        MessageResponse result = categoryService.deleteCategory("MyBusiness");

        assertThat(result.getMessage()).isEqualTo("Category deleted successfully");
        assertThat(customCategory.isDeleted()).isTrue();
        verify(categoryRepository).save(customCategory);
    }

    @Test
    @DisplayName("Delete: throws ForbiddenException when trying to delete a system category")
    void deleteCategory_systemCategory_throwsForbidden() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findAccessibleByNameAndUserId("Salary", 1L))
                .thenReturn(Optional.of(systemCategory));

        assertThatThrownBy(() -> categoryService.deleteCategory("Salary"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("cannot be deleted");
    }

    @Test
    @DisplayName("Delete: throws BadRequestException when category has active transactions")
    void deleteCategory_hasActiveTransactions_throwsBadRequest() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findAccessibleByNameAndUserId("MyBusiness", 1L))
                .thenReturn(Optional.of(customCategory));
        when(categoryRepository.hasActiveTransactions(2L)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.deleteCategory("MyBusiness"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("referenced by existing transactions");
    }

    @Test
    @DisplayName("Delete: throws ResourceNotFoundException when category not found")
    void deleteCategory_notFound_throwsNotFound() {
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(categoryRepository.findAccessibleByNameAndUserId("Unknown", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory("Unknown"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
