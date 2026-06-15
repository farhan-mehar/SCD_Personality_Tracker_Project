package com.psyche.controller;

import com.psyche.model.QuizHistory;
import com.psyche.model.User;
import com.psyche.repository.QuizAnswerRepository;
import com.psyche.repository.QuizHistoryRepository;
import com.psyche.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.psyche.config.LoginSuccessHandler;
import com.psyche.service.UserDetailsServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Explicit imports only — NO wildcard hamcrest to avoid any() ambiguity
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.argThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @MockBean private QuizHistoryRepository quizHistoryRepo;
    @MockBean private QuizAnswerRepository quizAnswerRepo;

    // ── Required so SecurityConfig's @Autowired fields resolve in this slice ──
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private LoginSuccessHandler loginSuccessHandler;
    @MockBean private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setQuizCompleted(false);
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
    }

    // ── GET /quiz ──────────────────────────────────────────────────
    @Test
    @WithMockUser(username = "test@example.com")
    void startQuiz_shows_firstQuestion() throws Exception {
        mockMvc.perform(get("/quiz"))
            .andExpect(status().isOk())
            .andExpect(view().name("quiz"))
            .andExpect(model().attribute("questionNum", 1))
            .andExpect(model().attribute("totalQ", 20));
    }

    // ── GET /quiz/question/{num} ───────────────────────────────────
    @Test
    @WithMockUser(username = "test@example.com")
    void showQuestion_validNum_returns200() throws Exception {
        mockMvc.perform(get("/quiz/question/5"))
            .andExpect(status().isOk())
            .andExpect(view().name("quiz"))
            .andExpect(model().attribute("questionNum", 5));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showQuestion_outOfRange_below_redirectsToDashboard() throws Exception {
        mockMvc.perform(get("/quiz/question/0"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showQuestion_outOfRange_above_redirectsToDashboard() throws Exception {
        mockMvc.perform(get("/quiz/question/21"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showQuestion_lastQuestion_setsIsLastTrue() throws Exception {
        mockMvc.perform(get("/quiz/question/20"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("isLast", true));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showQuestion_withError_passesErrorToModel() throws Exception {
        mockMvc.perform(get("/quiz/question/3").param("err", "Please select an answer"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showQuestion_withPreviousAnswer_setsPrevAnswer() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<Integer, Integer> answers = new HashMap<>();
        answers.put(3, 18);
        session.setAttribute("quizAnswers", answers);

        mockMvc.perform(get("/quiz/question/3").session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("prevAnswer", 18));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showQuestion_nullSession_createsNewAnswerMap() throws Exception {
        MockHttpSession session = new MockHttpSession();
        // no quizAnswers attribute set → null → should create new map
        mockMvc.perform(get("/quiz/question/2").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("quiz"));
    }

    // ── POST /quiz/question/{num} ──────────────────────────────────
    @Test
    @WithMockUser(username = "test@example.com")
    void submitAnswer_noAnswer_redirectsWithError() throws Exception {
        mockMvc.perform(post("/quiz/question/3").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("/quiz/question/3*"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void submitAnswer_validAnswer_redirectsToNext() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("quizAnswers", new HashMap<Integer, Integer>());

        mockMvc.perform(post("/quiz/question/5").with(csrf())
                .session(session)
                .param("answer", "18"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/quiz/question/6"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void submitAnswer_lastQuestion_redirectsToFinish() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("quizAnswers", new HashMap<Integer, Integer>());

        mockMvc.perform(post("/quiz/question/20").with(csrf())
                .session(session)
                .param("answer", "25"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/quiz/finish"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void submitAnswer_nullSession_createsNewMap() throws Exception {
        mockMvc.perform(post("/quiz/question/1").with(csrf())
                .param("answer", "12"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/quiz/question/2"));
    }

    // ── GET /quiz/finish ───────────────────────────────────────────
    @Test
    @WithMockUser(username = "test@example.com")
    void finishQuiz_incompleteAnswers_redirectsToQuiz() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<Integer, Integer> partial = new HashMap<>();
        for (int i = 1; i <= 5; i++) partial.put(i, 18);
        session.setAttribute("quizAnswers", partial);

        mockMvc.perform(get("/quiz/finish").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/quiz"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void finishQuiz_nullSession_redirectsToQuiz() throws Exception {
        mockMvc.perform(get("/quiz/finish"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/quiz"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void finishQuiz_allAnswers_savesScoresAndShowsResult() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<Integer, Integer> allAnswers = new HashMap<>();
        for (int i = 1; i <= 20; i++) allAnswers.put(i, 18);
        session.setAttribute("quizAnswers", allAnswers);

        when(quizHistoryRepo.countByUserId(1L)).thenReturn(0);
        when(quizHistoryRepo.findByUserIdOrderByAttemptNumberAsc(1L))
            .thenReturn(Collections.emptyList());
        when(userService.save(any(User.class))).thenReturn(user);

        mockMvc.perform(get("/quiz/finish").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("quiz-result"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attributeExists("scores"))
            .andExpect(model().attributeExists("weakTraits"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void finishQuiz_retake_includesPreviousHistory() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<Integer, Integer> allAnswers = new HashMap<>();
        for (int i = 1; i <= 20; i++) allAnswers.put(i, 25);
        session.setAttribute("quizAnswers", allAnswers);

        QuizHistory prev = new QuizHistory();
        prev.setAttemptNumber(1);
        prev.setOpenness(50);
        QuizHistory curr = new QuizHistory();
        curr.setAttemptNumber(2);
        curr.setOpenness(75);

        when(quizHistoryRepo.countByUserId(1L)).thenReturn(1);
        when(quizHistoryRepo.findByUserIdOrderByAttemptNumberAsc(1L))
            .thenReturn(List.of(prev, curr));
        when(userService.save(any(User.class))).thenReturn(user);

        mockMvc.perform(get("/quiz/finish").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("quiz-result"))
            .andExpect(model().attribute("isRetake", true))
            .andExpect(model().attributeExists("prevHistory"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void finishQuiz_scoreCappedAt100() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<Integer, Integer> allAnswers = new HashMap<>();
        for (int i = 1; i <= 20; i++) allAnswers.put(i, 25);
        session.setAttribute("quizAnswers", allAnswers);

        when(quizHistoryRepo.countByUserId(1L)).thenReturn(0);
        when(quizHistoryRepo.findByUserIdOrderByAttemptNumberAsc(1L))
            .thenReturn(Collections.emptyList());
        when(userService.save(any(User.class))).thenReturn(user);

        mockMvc.perform(get("/quiz/finish").session(session))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("scores"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void finishQuiz_lowScores_generateIntrovertMbti() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Map<Integer, Integer> allAnswers = new HashMap<>();
        // All score 0 → E=0(I), O=0(S), A=0(T), C=0(P) → ISTP
        for (int i = 1; i <= 20; i++) allAnswers.put(i, 0);
        session.setAttribute("quizAnswers", allAnswers);

        when(quizHistoryRepo.countByUserId(1L)).thenReturn(0);
        when(quizHistoryRepo.findByUserIdOrderByAttemptNumberAsc(1L))
            .thenReturn(Collections.emptyList());
        when(userService.save(any(User.class))).thenReturn(user);

        mockMvc.perform(get("/quiz/finish").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("quiz-result"));

        verify(userService).save(argThat(u -> "ISTP".equals(u.getMbtiType())));
    }
}
