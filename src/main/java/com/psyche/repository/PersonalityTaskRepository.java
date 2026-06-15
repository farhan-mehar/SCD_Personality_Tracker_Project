package com.psyche.repository;

import com.psyche.model.PersonalityTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PersonalityTaskRepository extends JpaRepository<PersonalityTask, Long> {

    List<PersonalityTask> findByTraitAndLevel(String trait, String level);

    @Query("SELECT p FROM PersonalityTask p WHERE p.trait = :trait AND :score BETWEEN p.minScore AND p.maxScore ORDER BY p.taskNumber")
    List<PersonalityTask> findByTraitAndScore(String trait, int score);
}
