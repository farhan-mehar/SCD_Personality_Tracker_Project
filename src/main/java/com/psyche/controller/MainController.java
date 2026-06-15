package com.psyche.controller;

import com.psyche.model.DailyTask;
import com.psyche.model.LoginSession;
import com.psyche.model.User;
import com.psyche.model.UserReward;
import com.psyche.service.HabitService;
import com.psyche.service.RewardService;
import com.psyche.repository.LoginSessionRepository;
import com.psyche.repository.QuizHistoryRepository;
import com.psyche.model.QuizHistory;
import com.psyche.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
public class MainController {

    @Autowired private UserService userService;
    @Autowired private HabitService habitService;
    @Autowired private RewardService rewardService;
    @Autowired private QuizHistoryRepository quizHistoryRepo;
    @Autowired private LoginSessionRepository sessionRepo;

    private User getUser(UserDetails ud) {
        return userService.findByEmail(ud.getUsername()).orElseThrow();
    }

    // ── Dashboard ──────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud,
                            HttpSession httpSession, Model m) {
        User user = getUser(ud);
        m.addAttribute("user", user);

        List<String> weakTraits = getWeakTraits(user);
        m.addAttribute("weakTraits", weakTraits);
        m.addAttribute("quizDone", user.isQuizCompleted());

        // Global streak
        int globalStreak = habitService.calculateGlobalStreak(user.getId());
        m.addAttribute("globalStreak",      globalStreak);
        m.addAttribute("globalStreakEmoji", habitService.streakEmoji(globalStreak));
        m.addAttribute("globalStreakMsg",   habitService.streakMsg(globalStreak));

        // Build trait cards
        List<Map<String, Object>> traitCards = new ArrayList<>();
        for (String trait : weakTraits) {
            List<DailyTask> tasks = habitService.getTodayTasks(user, trait);
            int pct = habitService.todayPercent(user.getId(), trait);
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("trait",    trait);
            card.put("color",    habitService.traitColor(trait));
            card.put("tasks",    tasks);
            card.put("todayPct", pct);
            traitCards.add(card);
        }

        int todayDone  = habitService.totalCompletedToday(user.getId());
        int todayTotal = habitService.totalTasksToday(user.getId());
        boolean allDone = todayTotal > 0 && todayDone == todayTotal && user.isQuizCompleted();

        m.addAttribute("traitCards",       traitCards);
        m.addAttribute("totalDoneToday",   todayDone);
        m.addAttribute("totalTasksToday",  todayTotal);
        m.addAttribute("allDoneToday",     allDone);
        m.addAttribute("allTimeCompleted", habitService.allTimeCompleted(user.getId()));

        // ── Login timer: seconds since login ──────────────────────
        LoginSession latestSession =
            sessionRepo.findTopByUserIdOrderByLoginAtDesc(user.getId()).orElse(null);
        long secondsSinceLogin = 0;
        if (latestSession != null) {
            secondsSinceLogin = ChronoUnit.SECONDS.between(
                latestSession.getLoginAt(), LocalDateTime.now());
        }
        // Deadline window: 10 min to earn speed reward. Show timer for 10 min.
        long timerWindowSecs = 10 * 60; // 10 minutes
        long secsRemaining   = Math.max(0, timerWindowSecs - secondsSinceLogin);
        m.addAttribute("timerSecsRemaining", secsRemaining);
        m.addAttribute("showSpeedTimer",     secsRemaining > 0 && !allDone);

        // Rewards count (for badge on nav)
        m.addAttribute("rewardCount", rewardService.getRewardCount(user.getId()));

        return "dashboard";
    }

    // ── Toggle task (AJAX) ─────────────────────────────────────────
    @PostMapping("/tasks/{id}/toggle")
    @ResponseBody
    public Map<String, Object> toggle(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails ud) {
        habitService.toggleTask(id);
        User user = getUser(ud);

        int totalDone  = habitService.totalCompletedToday(user.getId());
        int totalTasks = habitService.totalTasksToday(user.getId());
        boolean allDone = habitService.allTodayDone(user.getId());
        int streak = habitService.calculateGlobalStreak(user.getId());

        // Check & grant rewards when all tasks done
        List<UserReward> newRewards = new ArrayList<>();
        if (allDone) {
            newRewards = rewardService.onAllTasksCompleted(user);
        }

        // Build new reward info for front-end popup
        List<Map<String, String>> rewardData = new ArrayList<>();
        for (UserReward r : newRewards) {
            Map<String, String> rm = new LinkedHashMap<>();
            rm.put("emoji",       r.getEmoji());
            rm.put("title",       r.getTitle());
            rm.put("description", r.getDescription());
            rm.put("color",       r.getBadgeColor());
            rewardData.add(rm);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success",        true);
        resp.put("totalDoneToday", totalDone);
        resp.put("totalTasksToday",totalTasks);
        resp.put("allDoneToday",   allDone);
        resp.put("globalStreak",   streak);
        resp.put("streakEmoji",    habitService.streakEmoji(streak));
        resp.put("newRewards",     rewardData);
        resp.put("rewardCount",    rewardService.getRewardCount(user.getId()));
        return resp;
    }

    // ── Learn ──────────────────────────────────────────────────────
    @GetMapping("/learn")
    public String learn() { return "about"; }

    // ── Profile ────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails ud, Model m) {
        User user = getUser(ud);
        m.addAttribute("user", user);
        m.addAttribute("allTimeCompleted", habitService.allTimeCompleted(user.getId()));
        m.addAttribute("totalDoneToday",   habitService.totalCompletedToday(user.getId()));

        int globalStreak = habitService.calculateGlobalStreak(user.getId());
        m.addAttribute("globalStreak",      globalStreak);
        m.addAttribute("globalStreakEmoji", habitService.streakEmoji(globalStreak));

        List<Map<String, Object>> traitStats = new ArrayList<>();
        for (String t : List.of("Openness","Conscientiousness","Extraversion","Agreeableness","Neuroticism")) {
            int score = getScore(user, t);
            Map<String, Object> ts = new LinkedHashMap<>();
            ts.put("trait",      t);
            ts.put("color",      habitService.traitColor(t));
            ts.put("score",      score);
            ts.put("level",      score >= 70 ? "Strong" : score >= 40 ? "Moderate" : "Needs Work");
            ts.put("levelClass", score >= 70 ? "good"   : score >= 40 ? "mid"      : "low");
            traitStats.add(ts);
        }
        m.addAttribute("traitStats", traitStats);

        List<QuizHistory> quizHistory =
            quizHistoryRepo.findByUserIdOrderByAttemptNumberAsc(user.getId());
        m.addAttribute("quizHistory", quizHistory);

        // Rewards
        m.addAttribute("rewards",     rewardService.getRewards(user.getId()));
        m.addAttribute("rewardCount", rewardService.getRewardCount(user.getId()));

        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails ud,
                                @RequestParam String fullName,
                                RedirectAttributes ra) {
        User user = getUser(ud);
        if (!fullName.isBlank()) { user.setFullName(fullName); userService.save(user); }
        ra.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    // ── Helpers ────────────────────────────────────────────────────
    public static List<String> getWeakTraits(User user) {
        List<String> weak = new ArrayList<>();
        if (user.getOpenness()          < 60) weak.add("Openness");
        if (user.getConscientiousness() < 60) weak.add("Conscientiousness");
        if (user.getExtraversion()      < 60) weak.add("Extraversion");
        if (user.getAgreeableness()     < 60) weak.add("Agreeableness");
        if (user.getNeuroticism()       > 60) weak.add("Neuroticism");
        return weak;
    }

    private int getScore(User user, String trait) {
        return switch (trait) {
            case "Openness"          -> user.getOpenness();
            case "Conscientiousness" -> user.getConscientiousness();
            case "Extraversion"      -> user.getExtraversion();
            case "Agreeableness"     -> user.getAgreeableness();
            case "Neuroticism"       -> user.getNeuroticism();
            default                  -> 0;
        };
    }
}
