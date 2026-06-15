package com.psyche.service;

import com.psyche.model.DailyTask;
import com.psyche.model.User;
import com.psyche.repository.DailyTaskRepository;
import com.psyche.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired private JavaMailSender mailSender;
    @Autowired private UserRepository userRepo;
    @Autowired private HabitService habitService;
    @Autowired private DailyTaskRepository taskRepo;

    // Motivational messages — different for each reminder wave
    private static final String[][] WAVE_QUOTES = {
        { // Wave 1 — gentle start (5 min)
            "\"The secret of getting ahead is getting started.\" — Mark Twain",
            "\"Small daily improvements over time lead to stunning results.\" — Robin Sharma",
            "\"You don't have to be great to start, but you have to start to be great.\" — Zig Ziglar"
        },
        { // Wave 2 — energising (30 min)
            "\"Motivation is what gets you started. Habit is what keeps you going.\" — Jim Ryun",
            "\"Action is the foundational key to all success.\" — Pablo Picasso",
            "\"Do what you can, with what you have, where you are.\" — Theodore Roosevelt"
        },
        { // Wave 3 — urgency (2 hr)
            "\"The future depends on what you do today.\" — Mahatma Gandhi",
            "\"Don't watch the clock; do what it does. Keep going.\" — Sam Levenson",
            "\"Success is not final, failure is not fatal — it is the courage to continue that counts.\" — Churchill"
        },
        { // Wave 4 — final push (6 hr / evening)
            "\"Tonight's decisions are tomorrow's results. Choose wisely.\" — Psyche",
            "\"You are one task away from a perfect day. Don't let it slip.\" — Psyche",
            "\"Champions do the work even when they don't feel like it.\" — Psyche"
        }
    };

    // ── Daily reminder at 8 AM ─────────────────────────────────────
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyReminders() {
        List<User> users = userRepo.findAll();
        for (User user : users) {
            if (user.isQuizCompleted()) {
                try { sendDailyEmail(user); }
                catch (Exception e) {
                    System.err.println("Daily email failed: " + user.getEmail());
                }
            }
        }
    }

    // ── On login: fire 4 escalating reminder waves ─────────────────
    @Async
    public void sendLoginEmailAfterDelay(Long userId) {
        int[] delayMins  = { 5, 30, 120, 360 };   // 5min, 30min, 2hr, 6hr
        int[] waveNumber = { 1,  2,   3,   4  };

        try {
            for (int i = 0; i < delayMins.length; i++) {
                int wave     = waveNumber[i];
                int waitMins = (i == 0) ? delayMins[0]
                             : delayMins[i] - delayMins[i - 1];

                TimeUnit.MINUTES.sleep(waitMins);

                User user = userRepo.findById(userId).orElse(null);
                if (user == null || !user.isQuizCompleted()) return;

                List<DailyTask> todayTasks =
                    taskRepo.findByUserIdAndTaskDate(userId, LocalDate.now());
                boolean allDone = !todayTasks.isEmpty()
                    && todayTasks.stream().allMatch(DailyTask::isCompleted);

                if (allDone) {
                    // All done — send congrats and stop
                    sendCongratulationsEmail(user, todayTasks);
                    return;
                }

                // Send escalating reminder for this wave
                sendEscalatingReminderEmail(user, todayTasks, wave, delayMins[i]);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Login email error: " + e.getMessage());
        }
    }

    // ── Escalating reminder email with countdown timer ─────────────
    public void sendEscalatingReminderEmail(User user, List<DailyTask> tasks,
                                             int wave, int minutesSinceLogin) throws Exception {
        int streak    = habitService.calculateGlobalStreak(user.getId());
        String first  = firstName(user);
        String quote  = randomQuote(wave - 1);
        String today  = todayFormatted();
        String emoji  = habitService.streakEmoji(streak);

        List<DailyTask> pending = tasks.stream()
            .filter(t -> !t.isCompleted()).collect(Collectors.toList());
        List<DailyTask> done    = tasks.stream()
            .filter(DailyTask::isCompleted).collect(Collectors.toList());

        // Calculate deadline: tasks should be done by end of day
        LocalTime now         = LocalTime.now();
        int minsLeft          = (23 * 60 + 59) - (now.getHour() * 60 + now.getMinute());
        int hoursLeft         = minsLeft / 60;
        int remainMins        = minsLeft % 60;
        String timeLeftStr    = hoursLeft > 0
            ? hoursLeft + "h " + remainMins + "m left today"
            : remainMins + " minutes left today!";

        // Wave-specific styling
        String headerBg, headerTitle, urgencyEmoji, urgencyMsg;
        switch (wave) {
            case 1 -> {
                headerBg    = "linear-gradient(135deg,#6366f1,#8b5cf6)";
                headerTitle = "Hey " + first + ", your tasks are waiting! 👋";
                urgencyEmoji = "⏰";
                urgencyMsg  = "You just logged in — a perfect time to knock out your tasks!";
            }
            case 2 -> {
                headerBg    = "linear-gradient(135deg,#f97316,#ef4444)";
                headerTitle = first + ", 30 minutes have passed ⚡";
                urgencyEmoji = "🔔";
                urgencyMsg  = "Half an hour since login and tasks are still waiting — you've got this!";
            }
            case 3 -> {
                headerBg    = "linear-gradient(135deg,#dc2626,#b91c1c)";
                headerTitle = "⚠️ " + first + " — 2 hours gone!";
                urgencyEmoji = "🚨";
                urgencyMsg  = "Don't let today's tasks slip! Your streak is on the line.";
            }
            default -> {
                headerBg    = "linear-gradient(135deg,#1e293b,#334155)";
                headerTitle = "🌙 Last chance, " + first + "!";
                urgencyEmoji = "🌙";
                urgencyMsg  = "The day is almost over. Complete your tasks before midnight!";
            }
        }

        // Build pending tasks HTML
        String pendingColor = wave >= 3 ? "#dc2626" : "#f97316";
        StringBuilder pendingHtml = new StringBuilder();
        for (DailyTask t : pending) {
            pendingHtml.append(
                "<li style='margin-bottom:.6rem;padding:.75rem 1rem;background:#fff;" +
                "border-radius:8px;border-left:4px solid " + pendingColor + ";" +
                "color:#334155;font-size:.88rem;line-height:1.5;box-shadow:0 1px 3px rgba(0,0,0,.06)'>" +
                urgencyEmoji + " <strong>" + t.getTrait() + ":</strong> " + t.getTaskText() +
                "</li>");
        }

        // Done tasks
        String doneHtml = done.isEmpty() ? "" :
            "<div style='background:#f0fdf4;border:1px solid #86efac;border-radius:8px;" +
            "padding:.75rem 1rem;margin-bottom:.75rem;font-size:.84rem;color:#15803d'>" +
            "✅ Already done: <strong>" +
            done.stream().map(DailyTask::getTrait).collect(Collectors.joining(", ")) +
            "</strong></div>";

        // Countdown box
        String countdownHtml =
            "<div style='background:linear-gradient(135deg,#1e293b,#0f172a);border-radius:14px;" +
            "padding:1.25rem;margin:1rem 0;text-align:center'>" +
            "  <div style='color:rgba(255,255,255,.7);font-size:.72rem;text-transform:uppercase;" +
            "letter-spacing:.1em;margin-bottom:.4rem'>⏱ Time Remaining Today</div>" +
            "  <div style='font-size:1.9rem;font-weight:900;color:#fbbf24;letter-spacing:.05em'>" +
            hoursLeft + "h " + remainMins + "m</div>" +
            "  <div style='color:rgba(255,255,255,.6);font-size:.78rem;margin-top:.3rem'>" +
            "Tasks reset at midnight — act now!</div>" +
            "</div>";

        // Streak risk box
        String streakRiskHtml = streak > 0 ?
            "<div style='background:#fef9c3;border:1px solid #fde047;border-radius:10px;" +
            "padding:.85rem 1rem;margin-bottom:1rem;text-align:center'>" +
            "  <div style='font-size:1.5rem'>" + emoji + "</div>" +
            "  <div style='font-weight:800;color:#92400e;font-size:1.1rem'>" + streak + "-Day Streak at Risk!</div>" +
            "  <div style='color:#78350f;font-size:.8rem;margin-top:.25rem'>" +
            "Don't break it now — you've worked so hard!</div>" +
            "</div>" : "";

        // Wave label
        String waveLabel = "Reminder #" + wave + " of 4";

        String html =
            "<div style='font-family:\"Segoe UI\",Arial,sans-serif;max-width:580px;margin:0 auto;" +
            "background:#fff;border-radius:20px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1)'>" +
            // Header
            "  <div style='background:" + headerBg + ";padding:2rem;text-align:center'>" +
            "    <div style='font-size:2.5rem;margin-bottom:.4rem'>" + urgencyEmoji + "</div>" +
            "    <h1 style='color:#fff;margin:0;font-size:1.35rem;font-weight:700'>" + headerTitle + "</h1>" +
            "    <p style='color:rgba(255,255,255,.85);margin:.5rem 0 0;font-size:.85rem'>" + today + "</p>" +
            "    <div style='background:rgba(255,255,255,.15);display:inline-block;border-radius:100px;" +
            "padding:.25rem .9rem;font-size:.72rem;color:rgba(255,255,255,.9);margin-top:.6rem;" +
            "letter-spacing:.06em;text-transform:uppercase'>" + waveLabel + "</div>" +
            "  </div>" +
            // Body
            "  <div style='padding:1.75rem 2rem'>" +
            streakRiskHtml +
            "    <p style='color:#334155;font-size:.92rem;line-height:1.7;margin-bottom:1rem'>" +
            urgencyMsg + " You have <strong>" + pending.size() + " task(s)</strong> left.</p>" +
            countdownHtml +
            "    <h3 style='color:#1e293b;font-size:.9rem;margin:.85rem 0 .6rem'>📋 Pending Tasks:</h3>" +
            "    <ul style='list-style:none;padding:0;margin:0 0 .75rem'>" + pendingHtml + "</ul>" +
            doneHtml +
            "    <div style='background:#eef2ff;border-left:4px solid #6366f1;border-radius:0 8px 8px 0;" +
            "padding:.9rem 1.1rem;margin-bottom:1.5rem'>" +
            "      <p style='color:#4338ca;font-size:.87rem;margin:0;font-style:italic'>" + quote + "</p>" +
            "    </div>" +
            "    <div style='text-align:center'>" +
            "      <a href='http://localhost:8080/dashboard' style='background:" + headerBg + ";" +
            "color:#fff;padding:.9rem 2.25rem;border-radius:100px;text-decoration:none;" +
            "font-weight:700;font-size:.92rem;display:inline-block'>" +
            "        Complete Tasks Now →" +
            "      </a>" +
            "    </div>" +
            "  </div>" +
            "  <div style='background:#f8fafc;border-top:1px solid #e2e8f0;padding:.9rem;text-align:center'>" +
            "    <p style='color:#94a3b8;font-size:.72rem;margin:0'>✦ Psyche · " + waveLabel + " · " + timeLeftStr + "</p>" +
            "  </div>" +
            "</div>";

        // Escalating subject lines
        String subject = switch (wave) {
            case 1 -> "⏰ " + first + ", your daily tasks are waiting!";
            case 2 -> "🔔 " + first + " — 30 min passed, tasks still pending!";
            case 3 -> "🚨 " + first + " — 2 hours left, don't break your streak!";
            default -> "🌙 Last reminder: " + first + ", complete tasks before midnight!";
        };

        send(user.getEmail(), subject, html);
        System.out.println("✅ Wave " + wave + " email sent to: " + user.getEmail());
    }

    // ── Congratulations: all done ──────────────────────────────────
    public void sendCongratulationsEmail(User user, List<DailyTask> tasks) throws Exception {
        int streak   = habitService.calculateGlobalStreak(user.getId());
        String first = firstName(user);
        String emoji = habitService.streakEmoji(streak);
        String msg   = habitService.streakMsg(streak);

        // Check if speed reward earned
        String speedBonusHtml = "";
        // (reward check happens in RewardService, we just mention it)
        speedBonusHtml =
            "<div style='background:linear-gradient(135deg,#fef9c3,#fef3c7);border:2px solid #fde047;" +
            "border-radius:12px;padding:1rem;margin-bottom:1rem;text-align:center'>" +
            "  <div style='font-size:1.75rem'>⚡</div>" +
            "  <div style='font-weight:700;color:#92400e;font-size:.95rem'>Speed Bonus Unlocked!</div>" +
            "  <div style='color:#78350f;font-size:.8rem;margin-top:.2rem'>" +
            "Check your profile to see new rewards you've earned!</div>" +
            "</div>";

        String html =
            "<div style='font-family:\"Segoe UI\",Arial,sans-serif;max-width:580px;margin:0 auto;" +
            "background:#fff;border-radius:20px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1)'>" +
            "  <div style='background:linear-gradient(135deg,#16a34a,#22c55e);padding:2rem;text-align:center'>" +
            "    <div style='font-size:3rem;margin-bottom:.4rem'>🎉</div>" +
            "    <h1 style='color:#fff;margin:0;font-size:1.5rem;font-weight:700'>Amazing, " + first + "! All tasks done!</h1>" +
            "    <p style='color:rgba(255,255,255,.9);margin:.4rem 0 0;font-size:.88rem'>You showed up and delivered today!</p>" +
            "  </div>" +
            "  <div style='padding:1.75rem 2rem;text-align:center'>" +
            "    <div style='background:#f0fdf4;border:2px solid #86efac;border-radius:14px;padding:1.25rem;margin-bottom:1.25rem'>" +
            "      <div style='font-size:3rem'>" + emoji + "</div>" +
            "      <div style='font-size:2rem;font-weight:800;color:#16a34a'>" + streak + " Day Streak!</div>" +
            "      <div style='color:#15803d;font-style:italic;font-size:.88rem;margin-top:.3rem'>" + msg + "</div>" +
            "    </div>" +
            speedBonusHtml +
            "    <p style='color:#334155;line-height:1.7;font-size:.92rem;margin-bottom:1.5rem'>" +
            "Every single day you show up like this, you become a better version of yourself." +
            " <strong>Come back tomorrow to extend your streak!</strong> 🚀</p>" +
            "    <a href='http://localhost:8080/profile' style='background:linear-gradient(135deg,#16a34a,#22c55e);" +
            "color:#fff;padding:.9rem 2.25rem;border-radius:100px;text-decoration:none;font-weight:700;" +
            "font-size:.92rem;display:inline-block'>View Rewards & Progress →</a>" +
            "  </div>" +
            "  <div style='background:#f8fafc;border-top:1px solid #e2e8f0;padding:.9rem;text-align:center'>" +
            "    <p style='color:#94a3b8;font-size:.72rem;margin:0'>✦ Psyche Personality Tracker</p>" +
            "  </div>" +
            "</div>";

        send(user.getEmail(), "🎉 " + first + ", you crushed it! All tasks complete!", html);
        System.out.println("✅ Congrats email sent to: " + user.getEmail());
    }

    // ── Daily morning email at 8 AM ────────────────────────────────
    public void sendDailyEmail(User user) throws Exception {
        int streak   = habitService.calculateGlobalStreak(user.getId());
        String first = firstName(user);
        String emoji = habitService.streakEmoji(streak);
        String msg   = habitService.streakMsg(streak);
        String today = todayFormatted();
        String quote = randomQuote(0);

        String html =
            "<div style='font-family:\"Segoe UI\",Arial,sans-serif;max-width:580px;margin:0 auto;" +
            "background:#fff;border-radius:20px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08)'>" +
            "  <div style='background:linear-gradient(135deg,#6366f1,#8b5cf6,#a855f7);padding:2.5rem 2rem;text-align:center'>" +
            "    <div style='font-size:3rem;margin-bottom:.5rem'>" + emoji + "</div>" +
            "    <h1 style='color:#fff;margin:0;font-size:1.5rem;font-weight:700'>Good Morning, " + first + "!</h1>" +
            "    <p style='color:rgba(255,255,255,.85);margin:.5rem 0 0;font-size:.88rem'>" + today + "</p>" +
            "  </div>" +
            "  <div style='background:#f8fafc;padding:1.25rem;text-align:center;border-bottom:1px solid #f1f5f9'>" +
            "    <div style='display:inline-block;background:#fff;border:2px solid #e0e7ff;border-radius:14px;padding:.9rem 2rem'>" +
            "      <div style='font-size:2rem;font-weight:800;color:#6366f1'>" + streak + "</div>" +
            "      <div style='font-size:.72rem;color:#64748b;text-transform:uppercase;letter-spacing:.08em'>Day Streak</div>" +
            "      <div style='font-size:.82rem;color:#475569;font-style:italic;margin-top:.25rem'>" + msg + "</div>" +
            "    </div>" +
            "  </div>" +
            "  <div style='padding:1.75rem 2rem'>" +
            "    <p style='color:#334155;font-size:.92rem;line-height:1.7;margin-bottom:1.25rem'>" +
            "Your daily personality improvement tasks are ready! Complete them to keep your streak alive. 💪</p>" +
            "    <div style='background:#eef2ff;border-left:4px solid #6366f1;border-radius:0 10px 10px 0;" +
            "padding:1rem 1.25rem;margin-bottom:1.5rem'>" +
            "      <p style='color:#4338ca;font-size:.88rem;margin:0;font-style:italic;line-height:1.6'>" + quote + "</p>" +
            "    </div>" +
            "    <div style='text-align:center'>" +
            "      <a href='http://localhost:8080/dashboard' style='background:linear-gradient(135deg,#6366f1,#8b5cf6);" +
            "color:#fff;padding:1rem 2.5rem;border-radius:100px;text-decoration:none;font-weight:700;" +
            "font-size:.92rem;display:inline-block'>Go to Dashboard →</a>" +
            "    </div>" +
            "  </div>" +
            "  <div style='background:#f8fafc;border-top:1px solid #e2e8f0;padding:.9rem;text-align:center'>" +
            "    <p style='color:#94a3b8;font-size:.72rem;margin:0'>✦ Psyche · Daily reminder at 8:00 AM</p>" +
            "  </div>" +
            "</div>";

        send(user.getEmail(), "🔥 " + first + ", your daily task is ready — " + today, html);
    }

    // ── Old method kept for backward compat with LoginSuccessHandler
    @Async
    public void sendLoginEmailAfterDelay_legacy(Long userId) {
        sendLoginEmailAfterDelay(userId);
    }

    // ── Helpers ────────────────────────────────────────────────────
    private void send(String to, String subject, String html) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
        h.setTo(to);
        h.setSubject(subject);
        h.setText(html, true);
        mailSender.send(msg);
    }

    private String firstName(User user) {
        return user.getFullName().split(" ")[0];
    }

    private String todayFormatted() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy"));
    }

    private String randomQuote(int waveIdx) {
        String[] pool = WAVE_QUOTES[Math.min(waveIdx, WAVE_QUOTES.length - 1)];
        return pool[(int)(Math.random() * pool.length)];
    }
}
