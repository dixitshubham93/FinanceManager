package com.syfe.financemanager.repository;

import com.syfe.financemanager.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    @Query("SELECT g FROM SavingsGoal g WHERE g.user.id = :userId ORDER BY g.createdAt DESC")
    List<SavingsGoal> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT g FROM SavingsGoal g WHERE g.id = :id AND g.user.id = :userId")
    Optional<SavingsGoal> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
