package com.psyche.controller;

import com.psyche.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// addFilters = false  →  Spring Security's filter chain is skipped
// entirely for this test class (SecurityConfig isn't loaded in
// @WebMvcTest anyway, so this avoids the 401/default-security issue).
//
// NOTE: login.html / signup.html reference ${_csrf.parameterName} in
// their <input> tags. With filters disabled, the CsrfFilter never runs,
// so that attribute is never placed on the request → NPE during
// template rendering. `.with(csrf())` (from spring-security-test) sets
// the CsrfToken request attribute directly, independent of the filter
// chain, so the template can render even on GET requests.
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;

    // ── GET / redirects to login ───────────────────────────────────
    @Test
    void rootRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));
    }

    // ── GET /login ─────────────────────────────────────────────────
    @Test
    void loginPage_noParams_returns200() throws Exception {
        mockMvc.perform(get("/login").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("login"));
    }

    @Test
    void loginPage_withError_addsErrorAttribute() throws Exception {
        mockMvc.perform(get("/login").with(csrf()).param("error", ""))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("error"));
    }

    @Test
    void loginPage_withLogout_addsLogoutAttribute() throws Exception {
        mockMvc.perform(get("/login").with(csrf()).param("logout", ""))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("logout"));
    }

    // ── GET /signup ────────────────────────────────────────────────
    @Test
    void signupPage_returns200() throws Exception {
        mockMvc.perform(get("/signup").with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("signup"));
    }

    // ── POST /signup: blank fields ─────────────────────────────────
    @Test
    void signup_blankFields_redirectsWithError() throws Exception {
        mockMvc.perform(post("/signup")
                .param("fullName", "")
                .param("email", "")
                .param("password", "")
                .param("confirmPassword", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signup"));
    }

    // ── POST /signup: passwords mismatch ──────────────────────────
    @Test
    void signup_passwordsMismatch_redirectsWithError() throws Exception {
        mockMvc.perform(post("/signup")
                .param("fullName", "John Doe")
                .param("email", "john@example.com")
                .param("password", "pass123")
                .param("confirmPassword", "different"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signup"));
    }

    // ── POST /signup: password too short ──────────────────────────
    @Test
    void signup_passwordTooShort_redirectsWithError() throws Exception {
        mockMvc.perform(post("/signup")
                .param("fullName", "John Doe")
                .param("email", "john@example.com")
                .param("password", "abc")
                .param("confirmPassword", "abc"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signup"));
    }

    // ── POST /signup: email already exists ────────────────────────
    @Test
    void signup_emailExists_redirectsWithError() throws Exception {
        when(userService.emailExists("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/signup")
                .param("fullName", "Jane Doe")
                .param("email", "existing@example.com")
                .param("password", "validPass")
                .param("confirmPassword", "validPass"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signup"));
    }

    // ── POST /signup: successful registration ─────────────────────
    @Test
    void signup_success_redirectsToLogin() throws Exception {
        when(userService.emailExists("new@example.com")).thenReturn(false);

        mockMvc.perform(post("/signup")
                .param("fullName", "New User")
                .param("email", "new@example.com")
                .param("password", "validPass")
                .param("confirmPassword", "validPass"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        verify(userService).register("New User", "new@example.com", "validPass");
    }
}
