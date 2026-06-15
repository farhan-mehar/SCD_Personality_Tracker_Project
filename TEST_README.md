# Psyche — Test Suite Documentation

## Overview
117 JUnit 5 tests covering all service, controller, config, and model classes.  
JaCoCo is configured to enforce **80%+ instruction coverage** on `mvn verify`.

---

## How to Run

### Prerequisites
- Java 21+
- Maven 3.8+ (`mvn -version`)

### Run all tests
```bash
mvn test
```

### Run tests + generate JaCoCo coverage report
```bash
mvn verify
```

### View coverage report
After running `mvn verify`, open:
```
target/site/jacoco/index.html
```

### Run a specific test class
```bash
mvn test -Dtest=HabitServiceTest
```

---

## Test Structure

```
src/test/java/com/psyche/
├── model/
│   ├── UserTest.java                   (2 tests)  — User entity getters/setters
│   └── ModelEntitiesTest.java          (7 tests)  — DailyTask, QuizAnswer, QuizHistory,
│                                                     PersonalityTask, LoginSession, UserReward
├── service/
│   ├── HabitServiceTest.java          (26 tests)  — getTodayTasks, toggleTask, streaks, stats
│   ├── RewardServiceTest.java         (20 tests)  — all reward milestones, speed/streak/task rewards
│   ├── UserServiceTest.java            (6 tests)  — register, emailExists, findByEmail, save
│   ├── UserDetailsServiceImplTest.java (2 tests)  — loadUserByUsername success/failure
│   └── EmailServiceTest.java          (12 tests)  — all 4 wave emails, congrats, daily, scheduling
├── controller/
│   ├── AuthControllerTest.java        (10 tests)  — login, signup, all validation branches
│   ├── MainControllerTest.java        (11 tests)  — dashboard, toggle, profile, weak traits
│   └── QuizControllerTest.java        (18 tests)  — all questions, finish flow, MBTI derivation
└── config/
    └── LoginSuccessHandlerTest.java    (3 tests)  — login success handler branches
```

**Total: 117 tests**

---

## JaCoCo Configuration

The JaCoCo plugin in `pom.xml` is configured to:
1. **Instrument** bytecode at compile time (`prepare-agent`)
2. **Generate HTML/XML reports** after `mvn test` (`report` phase)
3. **Fail the build** if instruction coverage drops below 80% (`check` phase on `mvn verify`)

Excluded from coverage (infrastructure/bootstrap code):
- `com/psyche/config/DataInitializer.class`
- `com/psyche/PsycheApplication.class`

---

## Technology Stack (Tests)

| Library | Purpose |
|---------|---------|
| JUnit 5 | Test framework |
| Mockito | Mocking (`@Mock`, `@InjectMocks`, `@MockBean`) |
| Spring Boot Test | `@WebMvcTest`, `MockMvc` |
| Spring Security Test | `@WithMockUser`, CSRF |
| H2 | In-memory DB (replaces MySQL in tests) |
| JaCoCo 0.8.12 | Code coverage instrumentation & reports |
| AssertJ | Fluent assertions (`assertThat(...)`) |

---

## Test Configuration

`src/test/resources/application.properties` overrides the main config:
- Uses **H2 in-memory** database (no MySQL required)
- Mail host set to localhost (mocked in tests)
- `server.port=0` (random port)
- `spring.jpa.hibernate.ddl-auto=create-drop` (fresh schema per test run)
