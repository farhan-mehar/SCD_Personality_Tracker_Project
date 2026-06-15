package com.psyche.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_sessions")
public class LoginSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "tasks_completed_at")
    private LocalDateTime tasksCompletedAt;

    /** Minutes after login when all tasks were done (null if not done yet) */
    @Column(name = "minutes_to_complete")
    private Integer minutesToComplete;

    @PrePersist
    protected void onCreate() { loginAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getLoginAt() { return loginAt; }
    public void setLoginAt(LocalDateTime loginAt) { this.loginAt = loginAt; }
    public LocalDateTime getTasksCompletedAt() { return tasksCompletedAt; }
    public void setTasksCompletedAt(LocalDateTime t) { this.tasksCompletedAt = t; }
    public Integer getMinutesToComplete() { return minutesToComplete; }
    public void setMinutesToComplete(Integer m) { this.minutesToComplete = m; }
}
