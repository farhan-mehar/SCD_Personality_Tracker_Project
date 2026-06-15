package com.psyche.service;

import com.psyche.model.DailyTask;
import com.psyche.model.PersonalityTask;
import com.psyche.model.User;
import com.psyche.repository.DailyTaskRepository;
import com.psyche.repository.PersonalityTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class HabitService {

    @Autowired private DailyTaskRepository taskRepo;
    @Autowired private PersonalityTaskRepository personalityTaskRepo;

    // ── Get today's task for a trait (dynamically assigned, rotates daily) ──
    public List<DailyTask> getTodayTasks(User user, String trait) {
        LocalDate today = LocalDate.now();

        // Check if already created today
        List<DailyTask> existing = taskRepo.findByUserIdAndTaskDate(user.getId(), today)
                .stream().filter(t -> t.getTrait().equals(trait)).toList();
        if (!existing.isEmpty()) return existing;

        // Get score for this trait
        int score = getTraitScore(user, trait);

        // Fetch all tasks for this trait and score range from DB
        List<PersonalityTask> pool = personalityTaskRepo.findByTraitAndScore(trait, score);
        if (pool.isEmpty()) return new ArrayList<>();

        // Rotate: pick a different task each day based on day-of-year
        int dayOfYear  = today.getDayOfYear();
        int idx        = dayOfYear % pool.size();
        PersonalityTask picked = pool.get(idx);

        // Save today's task
        DailyTask dt = new DailyTask();
        dt.setUser(user);
        dt.setTrait(trait);
        dt.setTaskIndex(picked.getTaskNumber());
        dt.setTaskDate(today);
        dt.setCompleted(false);
        dt.setTaskText(picked.getTaskText());
        return Collections.singletonList(taskRepo.save(dt));
    }

    private int getTraitScore(User user, String trait) {
        switch (trait) {
            case "Openness":          return user.getOpenness();
            case "Conscientiousness": return user.getConscientiousness();
            case "Extraversion":      return user.getExtraversion();
            case "Agreeableness":     return user.getAgreeableness();
            case "Neuroticism":       return user.getNeuroticism();
            default:                  return 50;
        }
    }

    // ── Toggle task ────────────────────────────────────────────────
    public void toggleTask(Long taskId) {
        taskRepo.findById(taskId).ifPresent(t -> {
            t.setCompleted(!t.isCompleted());
            taskRepo.save(t);
        });
    }

    // ── Global streak: consecutive days where ANY task was done ───
    public int calculateGlobalStreak(Long userId) {
        int streak = 0;
        LocalDate date = LocalDate.now();
        for (int i = 0; i < 365; i++) {
            List<DailyTask> dayTasks = taskRepo.findByUserIdAndTaskDate(userId, date);
            if (dayTasks.isEmpty()) {
                if (i == 0) { date = date.minusDays(1); continue; }
                else break;
            }
            boolean anyDone = dayTasks.stream().anyMatch(DailyTask::isCompleted);
            if (anyDone) {
                streak++;
                date = date.minusDays(1);
            } else {
                if (i == 0) { date = date.minusDays(1); continue; }
                else break;
            }
        }
        return streak;
    }

    // ── Today stats ────────────────────────────────────────────────
    public int totalCompletedToday(Long userId) {
        return (int) taskRepo.findByUserIdAndTaskDate(userId, LocalDate.now())
                .stream().filter(DailyTask::isCompleted).count();
    }

    public int totalTasksToday(Long userId) {
        return taskRepo.findByUserIdAndTaskDate(userId, LocalDate.now()).size();
    }

    public int allTimeCompleted(Long userId) {
        return taskRepo.findByUserIdAndCompletedTrue(userId).size();
    }

    public int todayPercent(Long userId, String trait) {
        List<DailyTask> tasks = taskRepo
            .findByUserIdAndTaskDate(userId, LocalDate.now())
            .stream().filter(t -> t.getTrait().equals(trait)).toList();
        if (tasks.isEmpty()) return 0;
        long done = tasks.stream().filter(DailyTask::isCompleted).count();
        return (int)(done * 100 / tasks.size());
    }

    public boolean allTodayDone(Long userId) {
        List<DailyTask> today = taskRepo.findByUserIdAndTaskDate(userId, LocalDate.now());
        if (today.isEmpty()) return false;
        return today.stream().allMatch(DailyTask::isCompleted);
    }

    // ── Streak helpers ─────────────────────────────────────────────
    public String streakEmoji(int s) {
        if (s == 0) return "❄️";
        if (s < 3)  return "🌱";
        if (s < 7)  return "🔥";
        if (s < 14) return "⚡";
        if (s < 30) return "🌟";
        return "🏆";
    }

    public String streakMsg(int s) {
        if (s == 0)  return "Start your streak today!";
        if (s == 1)  return "Great start! Come back tomorrow!";
        if (s < 3)   return "Building momentum — keep going!";
        if (s < 7)   return "You're on fire — 1 week streak incoming!";
        if (s == 7)  return "🎉 ONE WEEK STREAK! You're amazing!";
        if (s < 14)  return "Unstoppable! Keep the habit alive!";
        if (s == 14) return "🎉 TWO WEEKS! Habit master unlocked!";
        if (s < 30)  return "Elite consistency — 30 days almost there!";
        return "🏆 LEGENDARY — 30+ day streak!";
    }

    public String traitColor(String trait) {
        switch (trait) {
            case "Openness":          return "#f97316";
            case "Conscientiousness": return "#3b82f6";
            case "Extraversion":      return "#eab308";
            case "Agreeableness":     return "#22c55e";
            case "Neuroticism":       return "#a855f7";
            default:                  return "#6366f1";
        }
    }
}
