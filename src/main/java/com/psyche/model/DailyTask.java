package com.psyche.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "daily_tasks")
public class DailyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String trait;
    private int taskIndex;
    private LocalDate taskDate;
    private boolean completed = false;

    @Column(length = 500)
    private String taskText;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getTrait() { return trait; }
    public void setTrait(String trait) { this.trait = trait; }
    public int getTaskIndex() { return taskIndex; }
    public void setTaskIndex(int taskIndex) { this.taskIndex = taskIndex; }
    public LocalDate getTaskDate() { return taskDate; }
    public void setTaskDate(LocalDate taskDate) { this.taskDate = taskDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getTaskText() { return taskText; }
    public void setTaskText(String taskText) { this.taskText = taskText; }
}
