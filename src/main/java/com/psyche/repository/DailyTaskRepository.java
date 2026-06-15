package com.psyche.repository;

import com.psyche.model.DailyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {
    List<DailyTask> findByUserIdAndTaskDate(Long userId, LocalDate date);
    List<DailyTask> findByUserIdAndTraitAndTaskDateBetween(Long userId, String trait, LocalDate from, LocalDate to);
    Optional<DailyTask> findByUserIdAndTraitAndTaskIndexAndTaskDate(Long userId, String trait, int idx, LocalDate date);
    List<DailyTask> findByUserId(Long userId);
    List<DailyTask> findByUserIdAndTrait(Long userId, String trait);
    List<DailyTask> findByUserIdAndCompletedTrue(Long userId);
}
