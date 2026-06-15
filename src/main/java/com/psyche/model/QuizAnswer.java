package com.psyche.model;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_answers")
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int questionNumber;
    private int answerValue;   // 0-25 score value
    private String trait;      // which Big Five trait this maps to

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(int questionNumber) { this.questionNumber = questionNumber; }
    public int getAnswerValue() { return answerValue; }
    public void setAnswerValue(int answerValue) { this.answerValue = answerValue; }
    public String getTrait() { return trait; }
    public void setTrait(String trait) { this.trait = trait; }
}
