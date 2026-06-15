package com.psyche.model;

import jakarta.persistence.*;

@Entity
@Table(name = "personality_tasks")
public class PersonalityTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trait;          // Openness, Conscientiousness, etc.

    // Score range this task applies to
    private int minScore;          // e.g. 0
    private int maxScore;          // e.g. 40 (low), 41-65 (moderate), 66-100 (high)

    private String level;          // "low", "moderate", "high"

    @Column(length = 600)
    private String taskText;       // The actual task description

    private int taskNumber;        // 1-10 per trait/level (rotates daily)

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrait() { return trait; }
    public void setTrait(String trait) { this.trait = trait; }
    public int getMinScore() { return minScore; }
    public void setMinScore(int minScore) { this.minScore = minScore; }
    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getTaskText() { return taskText; }
    public void setTaskText(String taskText) { this.taskText = taskText; }
    public int getTaskNumber() { return taskNumber; }
    public void setTaskNumber(int taskNumber) { this.taskNumber = taskNumber; }
}
