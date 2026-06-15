package com.psyche.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class ModelEntitiesTest {

    // ── DailyTask ──────────────────────────────────────────────────
    @Test
    void testDailyTask() {
        User user = new User();
        user.setId(1L);

        DailyTask task = new DailyTask();
        task.setId(10L);
        task.setUser(user);
        task.setTrait("Openness");
        task.setTaskIndex(3);
        LocalDate today = LocalDate.now();
        task.setTaskDate(today);
        task.setCompleted(false);
        task.setTaskText("Try a new activity today");

        assertThat(task.getId()).isEqualTo(10L);
        assertThat(task.getUser()).isEqualTo(user);
        assertThat(task.getTrait()).isEqualTo("Openness");
        assertThat(task.getTaskIndex()).isEqualTo(3);
        assertThat(task.getTaskDate()).isEqualTo(today);
        assertThat(task.isCompleted()).isFalse();
        assertThat(task.getTaskText()).isEqualTo("Try a new activity today");

        task.setCompleted(true);
        assertThat(task.isCompleted()).isTrue();
    }

    // ── QuizAnswer ─────────────────────────────────────────────────
    @Test
    void testQuizAnswer() {
        User user = new User();
        user.setId(2L);

        QuizAnswer qa = new QuizAnswer();
        qa.setId(5L);
        qa.setUser(user);
        qa.setQuestionNumber(7);
        qa.setAnswerValue(18);
        qa.setTrait("Extraversion");

        assertThat(qa.getId()).isEqualTo(5L);
        assertThat(qa.getUser()).isEqualTo(user);
        assertThat(qa.getQuestionNumber()).isEqualTo(7);
        assertThat(qa.getAnswerValue()).isEqualTo(18);
        assertThat(qa.getTrait()).isEqualTo("Extraversion");
    }

    // ── QuizHistory ────────────────────────────────────────────────
    @Test
    void testQuizHistory() {
        User user = new User();
        user.setId(3L);

        QuizHistory qh = new QuizHistory();
        qh.setId(1L);
        qh.setUser(user);
        qh.setAttemptNumber(2);
        qh.setOpenness(75);
        qh.setConscientiousness(65);
        qh.setExtraversion(80);
        qh.setAgreeableness(55);
        qh.setNeuroticism(40);
        qh.setMbtiType("ENFJ");
        LocalDateTime now = LocalDateTime.now();
        qh.setTakenAt(now);

        assertThat(qh.getId()).isEqualTo(1L);
        assertThat(qh.getUser()).isEqualTo(user);
        assertThat(qh.getAttemptNumber()).isEqualTo(2);
        assertThat(qh.getOpenness()).isEqualTo(75);
        assertThat(qh.getConscientiousness()).isEqualTo(65);
        assertThat(qh.getExtraversion()).isEqualTo(80);
        assertThat(qh.getAgreeableness()).isEqualTo(55);
        assertThat(qh.getNeuroticism()).isEqualTo(40);
        assertThat(qh.getMbtiType()).isEqualTo("ENFJ");
        assertThat(qh.getTakenAt()).isEqualTo(now);
    }

    // ── PersonalityTask ────────────────────────────────────────────
    @Test
    void testPersonalityTask() {
        PersonalityTask pt = new PersonalityTask();
        pt.setId(100L);
        pt.setTrait("Conscientiousness");
        pt.setMinScore(0);
        pt.setMaxScore(40);
        pt.setLevel("low");
        pt.setTaskText("Make a to-do list and complete 3 items");
        pt.setTaskNumber(5);

        assertThat(pt.getId()).isEqualTo(100L);
        assertThat(pt.getTrait()).isEqualTo("Conscientiousness");
        assertThat(pt.getMinScore()).isZero();
        assertThat(pt.getMaxScore()).isEqualTo(40);
        assertThat(pt.getLevel()).isEqualTo("low");
        assertThat(pt.getTaskText()).isEqualTo("Make a to-do list and complete 3 items");
        assertThat(pt.getTaskNumber()).isEqualTo(5);
    }

    // ── LoginSession ───────────────────────────────────────────────
    @Test
    void testLoginSession() {
        LoginSession ls = new LoginSession();
        ls.setId(20L);
        ls.setUserId(5L);
        LocalDateTime loginAt = LocalDateTime.now();
        ls.setLoginAt(loginAt);
        LocalDateTime completedAt = loginAt.plusMinutes(8);
        ls.setTasksCompletedAt(completedAt);
        ls.setMinutesToComplete(8);

        assertThat(ls.getId()).isEqualTo(20L);
        assertThat(ls.getUserId()).isEqualTo(5L);
        assertThat(ls.getLoginAt()).isEqualTo(loginAt);
        assertThat(ls.getTasksCompletedAt()).isEqualTo(completedAt);
        assertThat(ls.getMinutesToComplete()).isEqualTo(8);
    }

    @Test
    void testLoginSessionNullValues() {
        LoginSession ls = new LoginSession();
        assertThat(ls.getTasksCompletedAt()).isNull();
        assertThat(ls.getMinutesToComplete()).isNull();
    }

    // ── UserReward ─────────────────────────────────────────────────
    @Test
    void testUserReward() {
        User user = new User();
        user.setId(1L);

        UserReward reward = new UserReward();
        reward.setId(7L);
        reward.setUser(user);
        reward.setRewardKey("STREAK_7");
        reward.setTitle("Week Warrior");
        reward.setDescription("7-day streak achieved!");
        reward.setEmoji("⚡");
        reward.setBadgeColor("#eab308");
        LocalDateTime now = LocalDateTime.now();
        reward.setEarnedAt(now);

        assertThat(reward.getId()).isEqualTo(7L);
        assertThat(reward.getUser()).isEqualTo(user);
        assertThat(reward.getRewardKey()).isEqualTo("STREAK_7");
        assertThat(reward.getTitle()).isEqualTo("Week Warrior");
        assertThat(reward.getDescription()).isEqualTo("7-day streak achieved!");
        assertThat(reward.getEmoji()).isEqualTo("⚡");
        assertThat(reward.getBadgeColor()).isEqualTo("#eab308");
        assertThat(reward.getEarnedAt()).isEqualTo(now);
    }
}
