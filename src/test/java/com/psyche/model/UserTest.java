package com.psyche.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void testGettersAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPassword("encoded_pass");
        user.setMbtiType("INTJ");
        user.setOpenness(80);
        user.setConscientiousness(70);
        user.setExtraversion(60);
        user.setAgreeableness(90);
        user.setNeuroticism(30);
        user.setQuizCompleted(true);
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getFullName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded_pass");
        assertThat(user.getMbtiType()).isEqualTo("INTJ");
        assertThat(user.getOpenness()).isEqualTo(80);
        assertThat(user.getConscientiousness()).isEqualTo(70);
        assertThat(user.getExtraversion()).isEqualTo(60);
        assertThat(user.getAgreeableness()).isEqualTo(90);
        assertThat(user.getNeuroticism()).isEqualTo(30);
        assertThat(user.isQuizCompleted()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void testDefaultValues() {
        User user = new User();
        assertThat(user.getOpenness()).isZero();
        assertThat(user.getConscientiousness()).isZero();
        assertThat(user.getExtraversion()).isZero();
        assertThat(user.getAgreeableness()).isZero();
        assertThat(user.getNeuroticism()).isZero();
        assertThat(user.isQuizCompleted()).isFalse();
        assertThat(user.getDailyTasks()).isNull();
        assertThat(user.getQuizAnswers()).isNull();
    }
}
