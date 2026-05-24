package com.syfe.financemanager.repository;

import com.syfe.financemanager.entity.Transaction;
import com.syfe.financemanager.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.id = :id AND t.user.id = :userId AND t.isDeleted = false")
    Optional<Transaction> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user.id = :userId
              AND t.isDeleted = false
              AND t.date >= :startDate
              AND t.date <= :endDate
              AND t.category.name = :categoryName
            ORDER BY t.date DESC, t.createdAt DESC
            """)
    List<Transaction> findAllWithCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryName") String categoryName
    );

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user.id = :userId
              AND t.isDeleted = false
              AND t.date >= :startDate
              AND t.date <= :endDate
            ORDER BY t.date DESC, t.createdAt DESC
            """)
    List<Transaction> findAllWithoutCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user.id = :userId
              AND t.isDeleted = false
              AND t.category.type = :type
              AND t.date >= :startDate
            """)
    BigDecimal sumByUserIdAndTypeAndDateAfter(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate
    );

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user.id = :userId
              AND t.isDeleted = false
              AND YEAR(t.date) = :year
              AND MONTH(t.date) = :month
            """)
    List<Transaction> findByUserIdAndYearAndMonth(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user.id = :userId
              AND t.isDeleted = false
              AND YEAR(t.date) = :year
            """)
    List<Transaction> findByUserIdAndYear(
            @Param("userId") Long userId,
            @Param("year") int year
    );
}
