package com.psyche.repository;

import com.psyche.model.QuizHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizHistoryRepository extends JpaRepository<QuizHistory, Long> {
    List<QuizHistory> findByUserIdOrderByAttemptNumberAsc(Long userId);
    int countByUserId(Long userId);
}
