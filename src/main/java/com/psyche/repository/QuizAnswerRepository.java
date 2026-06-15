package com.psyche.repository;

import com.psyche.model.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM QuizAnswer q WHERE q.user.id = :userId")
    void deleteByUserId(Long userId);
}
