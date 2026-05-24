package com.syfe.financemanager.repository;

import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.isDeleted = false AND (c.user IS NULL OR c.user.id = :userId)")
    List<Category> findAllAccessibleByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.isDeleted = false AND (c.user IS NULL OR c.user.id = :userId)")
    Optional<Category> findAccessibleByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.user.id = :userId AND c.isDeleted = false AND c.isCustom = true")
    Optional<Category> findCustomByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.user.id = :userId AND c.isDeleted = false")
    boolean existsByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.user IS NULL")
    boolean existsByNameAndUserIsNull(@Param("name") String name);

    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.category.id = :categoryId AND t.isDeleted = false")
    boolean hasActiveTransactions(@Param("categoryId") Long categoryId);
}
