package com.psyche.controller;

import com.psyche.model.QuizAnswer;
import com.psyche.model.User;
import com.psyche.repository.QuizAnswerRepository;
import com.psyche.repository.QuizHistoryRepository;
import com.psyche.model.QuizHistory;
import com.psyche.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    @Autowired private UserService userService;
    @Autowired private QuizHistoryRepository quizHistoryRepo;
    @Autowired private QuizAnswerRepository quizAnswerRepo;

    // ── 20 Questions Definition ────────────────────────────────────
    private static final List<Map<String, Object>> QUESTIONS = new ArrayList<>();
    static {
        // Each question: text, options (label→value), trait
        addQ("I enjoy exploring new ideas and learning about topics I know nothing about.",
             "Openness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I have a vivid imagination and enjoy creative activities like art, music, or writing.",
             "Openness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I enjoy visiting new places and trying experiences I have never had before.",
             "Openness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I find abstract ideas and philosophical questions genuinely interesting.",
             "Openness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");

        addQ("I always complete my tasks on time and keep my promises.",
             "Conscientiousness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I keep my belongings and workspace neat and organized.",
             "Conscientiousness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I set clear goals and work hard every day to achieve them.",
             "Conscientiousness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I think carefully before making decisions — I rarely act on impulse.",
             "Conscientiousness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");

        addQ("I feel energized and recharged after spending time socializing with people.",
             "Extraversion", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I am comfortable being the center of attention in a group.",
             "Extraversion", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I easily start conversations with strangers and enjoy meeting new people.",
             "Extraversion", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I have a lot of enthusiasm and bring positive energy to the people around me.",
             "Extraversion", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");

        addQ("I genuinely care about other people's feelings and their wellbeing.",
             "Agreeableness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I avoid conflict and try to find common ground when disagreements arise.",
             "Agreeableness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I enjoy helping others even when it is not convenient for me.",
             "Agreeableness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I trust people easily and generally assume the best of their intentions.",
             "Agreeableness", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");

        addQ("I worry about things a lot, even when there is no clear reason to.",
             "Neuroticism", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I get stressed or anxious easily when things do not go as planned.",
             "Neuroticism", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("My mood changes frequently throughout the day depending on small things.",
             "Neuroticism", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
        addQ("I often feel anxious or nervous even without a specific reason I can identify.",
             "Neuroticism", "Strongly Agree:25", "Agree:18", "Neutral:12", "Disagree:6", "Strongly Disagree:0");
    }

    private static void addQ(String text, String trait, String... opts) {
        Map<String, Object> q = new LinkedHashMap<>();
        q.put("text", text);
        q.put("trait", trait);
        List<Map<String, Object>> options = new ArrayList<>();
        for (String opt : opts) {
            String[] parts = opt.split(":");
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("label", parts[0]);
            o.put("value", Integer.parseInt(parts[1]));
            options.add(o);
        }
        q.put("options", options);
        QUESTIONS.add(q);
    }

    // ── Start quiz ─────────────────────────────────────────────────
    @GetMapping
    public String startQuiz(@AuthenticationPrincipal UserDetails ud,
                            HttpSession session, Model m) {
        User user = userService.findByEmail(ud.getUsername()).orElseThrow();
        session.setAttribute("quizAnswers", new HashMap<Integer, Integer>());
        return showQuestion(1, null, m, session);
    }

    // ── Show a question ────────────────────────────────────────────
    @GetMapping("/question/{num}")
    public String showQuestionGet(@PathVariable int num,
                                  @RequestParam(required=false) String err,
                                  HttpSession session, Model m) {
        return showQuestion(num, err, m, session);
    }

    private String showQuestion(int num, String err, Model m, HttpSession session) {
        if (num < 1 || num > 20) return "redirect:/dashboard";

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> saved = (Map<Integer, Integer>)
            session.getAttribute("quizAnswers");
        if (saved == null) {
            saved = new HashMap<>();
            session.setAttribute("quizAnswers", saved);
        }

        Map<String, Object> q = QUESTIONS.get(num - 1);
        m.addAttribute("question",     q);
        m.addAttribute("questionNum",  num);
        m.addAttribute("totalQ",       20);
        m.addAttribute("progress",     (int)((num - 1) * 100.0 / 20));
        m.addAttribute("isLast",       num == 20);
        m.addAttribute("error",        err);
        m.addAttribute("prevAnswer",   saved.get(num));
        return "quiz";
    }

    // ── Submit answer + go next ────────────────────────────────────
    @PostMapping("/question/{num}")
    public String submitAnswer(@PathVariable int num,
                               @RequestParam(required=false) Integer answer,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (answer == null) {
            return "redirect:/quiz/question/" + num + "?err=You+must+select+an+answer+before+proceeding.";
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> saved = (Map<Integer, Integer>)
            session.getAttribute("quizAnswers");
        if (saved == null) saved = new HashMap<>();
        saved.put(num, answer);
        session.setAttribute("quizAnswers", saved);

        if (num < 20) {
            return "redirect:/quiz/question/" + (num + 1);
        } else {
            return "redirect:/quiz/finish";
        }
    }

    // ── Finish quiz — calculate + save scores ──────────────────────
    @Transactional
    @GetMapping("/finish")
    public String finishQuiz(@AuthenticationPrincipal UserDetails ud,
                             HttpSession session, Model m) {
        @SuppressWarnings("unchecked")
        Map<Integer, Integer> saved = (Map<Integer, Integer>)
            session.getAttribute("quizAnswers");
        if (saved == null || saved.size() < 20) return "redirect:/quiz";

        User user = userService.findByEmail(ud.getUsername()).orElseThrow();

        // Delete old answers
        quizAnswerRepo.deleteByUserId(user.getId());

        // Tally scores per trait
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Openness", 0);
        scores.put("Conscientiousness", 0);
        scores.put("Extraversion", 0);
        scores.put("Agreeableness", 0);
        scores.put("Neuroticism", 0);

        for (int i = 1; i <= 20; i++) {
            String trait = (String) QUESTIONS.get(i - 1).get("trait");
            int val = saved.getOrDefault(i, 0);
            scores.put(trait, scores.get(trait) + val);
            // Save each answer to DB
            QuizAnswer qa = new QuizAnswer();
            qa.setUser(user);
            qa.setQuestionNumber(i);
            qa.setAnswerValue(val);
            qa.setTrait(trait);
            quizAnswerRepo.save(qa);
        }

        // Update user trait scores (max 100 each: 4 questions x 25)
        int o = Math.min(100, scores.get("Openness"));
        int c = Math.min(100, scores.get("Conscientiousness"));
        int e = Math.min(100, scores.get("Extraversion"));
        int a = Math.min(100, scores.get("Agreeableness"));
        int n = Math.min(100, scores.get("Neuroticism"));
        user.setOpenness(o);
        user.setConscientiousness(c);
        user.setExtraversion(e);
        user.setAgreeableness(a);
        user.setNeuroticism(n);
        // Derive MBTI from scores
        String mbti = (e >= 50 ? "E" : "I")
                    + (o >= 50 ? "N" : "S")
                    + (a >= 50 ? "F" : "T")
                    + (c >= 50 ? "J" : "P");
        user.setMbtiType(mbti);
        // Save quiz history for progress comparison
        QuizHistory hist = new QuizHistory();
        hist.setUser(user);
        hist.setAttemptNumber(quizHistoryRepo.countByUserId(user.getId()) + 1);
        hist.setOpenness(o);
        hist.setConscientiousness(c);
        hist.setExtraversion(e);
        hist.setAgreeableness(a);
        hist.setNeuroticism(n);
        hist.setMbtiType(mbti);
        quizHistoryRepo.save(hist);

        user.setQuizCompleted(true);
        userService.save(user);

        session.removeAttribute("quizAnswers");

        // Pass history for progress comparison
        java.util.List<QuizHistory> history = quizHistoryRepo.findByUserIdOrderByAttemptNumberAsc(user.getId());
        m.addAttribute("user",        user);
        m.addAttribute("scores",      scores);
        m.addAttribute("weakTraits",  MainController.getWeakTraits(user));
        m.addAttribute("history",     history);
        m.addAttribute("isRetake",    history.size() > 1);
        if (history.size() >= 2) {
            QuizHistory prev = history.get(history.size() - 2);
            m.addAttribute("prevHistory", prev);
        }
        return "quiz-result";
    }
}
