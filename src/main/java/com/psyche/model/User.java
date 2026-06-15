package com.psyche.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String fullName;

    @NotBlank @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    private String mbtiType;
    private int openness          = 0;
    private int conscientiousness = 0;
    private int extraversion      = 0;
    private int agreeableness     = 0;
    private int neuroticism       = 0;

    private boolean quizCompleted = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DailyTask> dailyTasks;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizAnswer> quizAnswers;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getMbtiType() { return mbtiType; }
    public void setMbtiType(String mbtiType) { this.mbtiType = mbtiType; }
    public int getOpenness() { return openness; }
    public void setOpenness(int openness) { this.openness = openness; }
    public int getConscientiousness() { return conscientiousness; }
    public void setConscientiousness(int v) { this.conscientiousness = v; }
    public int getExtraversion() { return extraversion; }
    public void setExtraversion(int v) { this.extraversion = v; }
    public int getAgreeableness() { return agreeableness; }
    public void setAgreeableness(int v) { this.agreeableness = v; }
    public int getNeuroticism() { return neuroticism; }
    public void setNeuroticism(int v) { this.neuroticism = v; }
    public boolean isQuizCompleted() { return quizCompleted; }
    public void setQuizCompleted(boolean quizCompleted) { this.quizCompleted = quizCompleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<DailyTask> getDailyTasks() { return dailyTasks; }
    public List<QuizAnswer> getQuizAnswers() { return quizAnswers; }
}
