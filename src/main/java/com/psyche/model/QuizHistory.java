package com.psyche.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_history")
public class QuizHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int attemptNumber;
    private int openness;
    private int conscientiousness;
    private int extraversion;
    private int agreeableness;
    private int neuroticism;
    private String mbtiType;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @PrePersist
    protected void onCreate() { takenAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }
    public int getOpenness() { return openness; }
    public void setOpenness(int openness) { this.openness = openness; }
    public int getConscientiousness() { return conscientiousness; }
    public void setConscientiousness(int conscientiousness) { this.conscientiousness = conscientiousness; }
    public int getExtraversion() { return extraversion; }
    public void setExtraversion(int extraversion) { this.extraversion = extraversion; }
    public int getAgreeableness() { return agreeableness; }
    public void setAgreeableness(int agreeableness) { this.agreeableness = agreeableness; }
    public int getNeuroticism() { return neuroticism; }
    public void setNeuroticism(int neuroticism) { this.neuroticism = neuroticism; }
    public String getMbtiType() { return mbtiType; }
    public void setMbtiType(String mbtiType) { this.mbtiType = mbtiType; }
    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }
}
