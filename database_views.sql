-- ═══════════════════════════════════════════════════════════
--  PSYCHE DATABASE — Clean Views for phpMyAdmin
--  Run these in phpMyAdmin → SQL tab on personality_db
-- ═══════════════════════════════════════════════════════════

-- ── VIEW 1: Users Overview ─────────────────────────────────
-- Shows all users with their personality info cleanly
CREATE OR REPLACE VIEW view_users_overview AS
SELECT
    u.id            AS 'User ID',
    u.full_name     AS 'Full Name',
    u.email         AS 'Email',
    u.mbti_type     AS 'MBTI Type',
    CASE WHEN u.quiz_completed = 1 THEN '✓ Done' ELSE '✗ Pending' END
                    AS 'Quiz Status',
    u.openness      AS 'Openness',
    u.conscientiousness AS 'Conscientiousness',
    u.extraversion  AS 'Extraversion',
    u.agreeableness AS 'Agreeableness',
    u.neuroticism   AS 'Neuroticism',
    DATE_FORMAT(u.created_at, '%d %b %Y %H:%i') AS 'Joined On'
FROM users u
ORDER BY u.created_at DESC;

-- ── VIEW 2: Daily Tasks Summary ────────────────────────────
-- Shows each user's tasks per day, nicely ordered
CREATE OR REPLACE VIEW view_daily_tasks AS
SELECT
    u.full_name                     AS 'User',
    u.email                         AS 'Email',
    dt.trait                        AS 'Trait',
    dt.task_date                    AS 'Date',
    LEFT(dt.task_text, 80)          AS 'Task (Preview)',
    CASE WHEN dt.completed = 1 THEN '✅ Done' ELSE '⏳ Pending' END
                                    AS 'Status'
FROM daily_tasks dt
JOIN users u ON dt.user_id = u.id
ORDER BY dt.task_date DESC, u.full_name, dt.trait;

-- ── VIEW 3: Streak Summary per User ───────────────────────
-- Shows how many tasks each user has completed per day
CREATE OR REPLACE VIEW view_user_progress AS
SELECT
    u.full_name                     AS 'User',
    u.email                         AS 'Email',
    u.mbti_type                     AS 'MBTI',
    dt.task_date                    AS 'Date',
    COUNT(dt.id)                    AS 'Total Tasks',
    SUM(CASE WHEN dt.completed=1 THEN 1 ELSE 0 END) AS 'Completed',
    CONCAT(
      ROUND(SUM(CASE WHEN dt.completed=1 THEN 1 ELSE 0 END)
            * 100.0 / COUNT(dt.id)), '%'
    )                               AS 'Completion %'
FROM daily_tasks dt
JOIN users u ON dt.user_id = u.id
GROUP BY u.id, u.full_name, u.email, u.mbti_type, dt.task_date
ORDER BY dt.task_date DESC, u.full_name;

-- ── VIEW 4: Quiz History Comparison ───────────────────────
CREATE OR REPLACE VIEW view_quiz_history AS
SELECT
    u.full_name     AS 'User',
    u.email         AS 'Email',
    qh.attempt_number AS 'Attempt #',
    qh.mbti_type    AS 'MBTI',
    qh.openness     AS 'Openness',
    qh.conscientiousness AS 'Conscientiousness',
    qh.extraversion AS 'Extraversion',
    qh.agreeableness AS 'Agreeableness',
    qh.neuroticism  AS 'Neuroticism',
    DATE_FORMAT(qh.taken_at, '%d %b %Y %H:%i') AS 'Taken On'
FROM quiz_history qh
JOIN users u ON qh.user_id = u.id
ORDER BY u.full_name, qh.attempt_number;

-- ═══════════════════════════════════════════════════════════
--  HOW TO USE:
--  1. Open phpMyAdmin → select personality_db
--  2. Click "SQL" tab at the top
--  3. Paste this entire script and click "Go"
--  4. After that, you will see these views in the left sidebar:
--     • view_users_overview
--     • view_daily_tasks
--     • view_user_progress
--     • view_quiz_history
--  5. Click any view → it shows clean organized data!
-- ═══════════════════════════════════════════════════════════
