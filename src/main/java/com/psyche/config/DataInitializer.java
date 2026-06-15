package com.psyche.config;

import com.psyche.model.PersonalityTask;
import com.psyche.repository.PersonalityTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PersonalityTaskRepository taskRepo;

    @Override
    public void run(String... args) {
        // Only seed if empty
        if (taskRepo.count() > 0) return;

        List<PersonalityTask> tasks = new ArrayList<>();

        // ── OPENNESS ─────────────────────────────────────────────
        // Low (0-39): needs creative exploration
        tasks.add(t("Openness", 0, 39, "low", 1, "Visit a place in your city you have never been to before — a park, museum, or street market."));
        tasks.add(t("Openness", 0, 39, "low", 2, "Pick up a book from a genre you have never read — read just 10 pages today."));
        tasks.add(t("Openness", 0, 39, "low", 3, "Try cooking one dish from a cuisine you have never cooked before."));
        tasks.add(t("Openness", 0, 39, "low", 4, "Watch a documentary on a topic completely outside your usual interests."));
        tasks.add(t("Openness", 0, 39, "low", 5, "Write 5 things you are curious about but have never explored — pick one to research today."));
        tasks.add(t("Openness", 0, 39, "low", 6, "Talk to someone from a very different background and ask about their life experiences."));
        tasks.add(t("Openness", 0, 39, "low", 7, "Try a creative hobby for 20 minutes — drawing, writing, photography, or music."));
        tasks.add(t("Openness", 0, 39, "low", 8, "Change your daily routine in one small way — take a different route, eat at a new place."));
        tasks.add(t("Openness", 0, 39, "low", 9, "Listen to a genre of music you have never intentionally listened to before."));
        tasks.add(t("Openness", 0, 39, "low", 10, "Learn 10 words in a new language and use them in sentences today."));

        // Moderate (40-65): can push further
        tasks.add(t("Openness", 40, 65, "moderate", 1, "Sign up for a free online class on a topic you know nothing about — spend 30 min on it."));
        tasks.add(t("Openness", 40, 65, "moderate", 2, "Start a daily journal — write for 10 minutes about your thoughts, ideas, or imagination."));
        tasks.add(t("Openness", 40, 65, "moderate", 3, "Attend a cultural event, exhibit, or performance you would not normally choose."));
        tasks.add(t("Openness", 40, 65, "moderate", 4, "Challenge one of your existing beliefs — research the opposing viewpoint with an open mind."));
        tasks.add(t("Openness", 40, 65, "moderate", 5, "Brainstorm 10 creative solutions to a problem in your life — no idea is too wild."));
        tasks.add(t("Openness", 40, 65, "moderate", 6, "Spend 20 minutes exploring a field of knowledge you have mild interest in but never dived into."));
        tasks.add(t("Openness", 40, 65, "moderate", 7, "Write a short story (even just one paragraph) about an imaginary world or scenario."));
        tasks.add(t("Openness", 40, 65, "moderate", 8, "Have a conversation about philosophy, ethics, or big questions with someone today."));
        tasks.add(t("Openness", 40, 65, "moderate", 9, "Redesign your workspace or rearrange a room to feel more creative and inspiring."));
        tasks.add(t("Openness", 40, 65, "moderate", 10, "Find an artist, musician, or writer you have never heard of and explore their work today."));

        // ── CONSCIENTIOUSNESS ─────────────────────────────────────
        // Low (0-39): needs structure
        tasks.add(t("Conscientiousness", 0, 39, "low", 1, "Make a written to-do list for today right now — 3 must-do tasks. Do them before anything else."));
        tasks.add(t("Conscientiousness", 0, 39, "low", 2, "Set a timer for 25 minutes and work on ONE task with zero phone or tab switching."));
        tasks.add(t("Conscientiousness", 0, 39, "low", 3, "Clean and organize your desk or study area completely before starting work today."));
        tasks.add(t("Conscientiousness", 0, 39, "low", 4, "Identify one task you have been procrastinating for over 3 days — do just the first step today."));
        tasks.add(t("Conscientiousness", 0, 39, "low", 5, "Plan tomorrow evening tonight — write down 3 goals, 3 tasks, and your wake-up time."));
        tasks.add(t("Conscientiousness", 0, 39, "low", 6, "Download a habit tracking app and track at least one habit today — water, sleep, or exercise."));
        tasks.add(t("Conscientiousness", 0, 39, "low", 7, "Set phone reminders for your 3 most important tasks today and honor each one."));
        tasks.add(t("Conscientiousness", 0, 39, "low", 8, "Review your week: list what you completed and what you missed, then plan how to improve."));
        tasks.add(t("Conscientiousness", 0, 39, "low", 9, "Keep your phone in another room for 2 hours while you work on something meaningful."));
        tasks.add(t("Conscientiousness", 0, 39, "low", 10, "Commit to sleeping at a fixed time tonight and waking up at a fixed time tomorrow."));

        // Moderate (40-65)
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 1, "Use time blocking today — assign specific times to specific tasks and stick to the schedule."));
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 2, "Complete the hardest task on your list first thing in the morning before anything else."));
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 3, "Set a monthly goal and break it into 4 weekly milestones — write it down today."));
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 4, "Do a weekly review: what worked, what did not, and what you will do differently next week."));
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 5, "Limit social media to exactly 20 minutes today — set a timer and stop when it rings."));
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 6, "Prepare everything you need for tomorrow tonight — clothes, bag, meal prep, schedule."));
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 7, "Work in 3 focused 25-minute Pomodoro sessions today with 5-minute breaks in between."));
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 8, "Track your time today — write what you did every hour. Review where time was wasted."));
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 9, "Say no to one non-essential commitment or distraction today to protect your focus time."));
        tasks.add(t("Conscientiousness", 40, 65, "moderate", 10, "Batch similar tasks together — answer all emails at once, make all calls at once."));

        // ── EXTRAVERSION ──────────────────────────────────────────
        // Low (0-39): introverted, needs gentle social push
        tasks.add(t("Extraversion", 0, 39, "low", 1, "Say hello and smile at one new person today — a classmate, neighbor, or cashier counts."));
        tasks.add(t("Extraversion", 0, 39, "low", 2, "Text or call one person you have not spoken to in over 2 weeks — just to check in."));
        tasks.add(t("Extraversion", 0, 39, "low", 3, "Share one opinion or idea clearly in a group conversation today — your voice matters."));
        tasks.add(t("Extraversion", 0, 39, "low", 4, "Spend 30 minutes in a public place like a café or library — just being around people."));
        tasks.add(t("Extraversion", 0, 39, "low", 5, "Ask someone a genuine question about their day or their interests — listen fully."));
        tasks.add(t("Extraversion", 0, 39, "low", 6, "Join one online or local group or club related to something you already enjoy."));
        tasks.add(t("Extraversion", 0, 39, "low", 7, "Give one specific, genuine compliment to someone today and observe their reaction."));
        tasks.add(t("Extraversion", 0, 39, "low", 8, "Accept one social invitation today that you would normally decline or avoid."));
        tasks.add(t("Extraversion", 0, 39, "low", 9, "Introduce yourself to one person you see regularly but have never spoken to."));
        tasks.add(t("Extraversion", 0, 39, "low", 10, "Speak up at least twice during a group discussion, class, or meeting today."));

        // Moderate (40-65)
        tasks.add(t("Extraversion", 40, 65, "moderate", 1, "Initiate a social plan — invite someone for coffee, a walk, or a call this week."));
        tasks.add(t("Extraversion", 40, 65, "moderate", 2, "Attend a social event or gathering you have been putting off — commit to staying 30 min."));
        tasks.add(t("Extraversion", 40, 65, "moderate", 3, "Lead a conversation today — ask questions, keep it going, and make the other person feel heard."));
        tasks.add(t("Extraversion", 40, 65, "moderate", 4, "Volunteer to present, speak, or lead something in class, work, or a group today."));
        tasks.add(t("Extraversion", 40, 65, "moderate", 5, "Network — reach out to someone you admire or want to connect with professionally or personally."));
        tasks.add(t("Extraversion", 40, 65, "moderate", 6, "Call (not text) a friend or family member and have a proper conversation for at least 15 min."));
        tasks.add(t("Extraversion", 40, 65, "moderate", 7, "Share something creative or personal on social media or with a friend — a thought, photo, or idea."));
        tasks.add(t("Extraversion", 40, 65, "moderate", 8, "Organize a small get-together, study session, or activity with 2-3 people this week."));
        tasks.add(t("Extraversion", 40, 65, "moderate", 9, "Practice storytelling — share one interesting or funny experience from your week with someone."));
        tasks.add(t("Extraversion", 40, 65, "moderate", 10, "Make eye contact and smile at 5 different people throughout your day today."));

        // ── AGREEABLENESS ─────────────────────────────────────────
        // Low (0-39): needs empathy and kindness practice
        tasks.add(t("Agreeableness", 0, 39, "low", 1, "Practice active listening today — in one conversation, put your phone away and give full attention."));
        tasks.add(t("Agreeableness", 0, 39, "low", 2, "Do one completely selfless act today — help someone without any expectation of return."));
        tasks.add(t("Agreeableness", 0, 39, "low", 3, "When you disagree today, pause and say 'I understand where you are coming from' before responding."));
        tasks.add(t("Agreeableness", 0, 39, "low", 4, "Write a genuine thank-you message to someone who has helped or supported you recently."));
        tasks.add(t("Agreeableness", 0, 39, "low", 5, "Ask someone how they are truly doing — and listen to the full answer without interrupting."));
        tasks.add(t("Agreeableness", 0, 39, "low", 6, "Apologize to someone you may have hurt or been dismissive toward — mean it fully."));
        tasks.add(t("Agreeableness", 0, 39, "low", 7, "Volunteer for a small task that benefits others — at home, school, work, or community."));
        tasks.add(t("Agreeableness", 0, 39, "low", 8, "Identify one relationship that needs repair — take one step to improve it today."));
        tasks.add(t("Agreeableness", 0, 39, "low", 9, "Before judging someone's action today, ask yourself 'what might have led them to do this?'"));
        tasks.add(t("Agreeableness", 0, 39, "low", 10, "Offer to help someone with something they are struggling with — even if small."));

        // Moderate (40-65)
        tasks.add(t("Agreeableness", 40, 65, "moderate", 1, "Mediate or smooth over a conflict or tension between two people in your life today."));
        tasks.add(t("Agreeableness", 40, 65, "moderate", 2, "Practice gratitude — write 5 things you appreciate about 2 different people in your life."));
        tasks.add(t("Agreeableness", 40, 65, "moderate", 3, "Go out of your way to make someone's day better — surprise them with a kind gesture."));
        tasks.add(t("Agreeableness", 40, 65, "moderate", 4, "In one disagreement today, try to find the truth in the other person's perspective first."));
        tasks.add(t("Agreeableness", 40, 65, "moderate", 5, "Spend 15 minutes writing about someone you find difficult — try to understand their struggles."));
        tasks.add(t("Agreeableness", 40, 65, "moderate", 6, "Donate, volunteer, or do something kind for someone outside your immediate circle today."));
        tasks.add(t("Agreeableness", 40, 65, "moderate", 7, "Be the peacemaker today — if tension arises, de-escalate calmly rather than feeding it."));
        tasks.add(t("Agreeableness", 40, 65, "moderate", 8, "Pay 3 sincere, specific compliments to 3 different people throughout your day."));
        tasks.add(t("Agreeableness", 40, 65, "moderate", 9, "Check in on someone who has been going through a hard time — offer support or just listen."));
        tasks.add(t("Agreeableness", 40, 65, "moderate", 10, "Reflect tonight: in what moments today were you impatient or unkind? How can you do better?"));

        // ── NEUROTICISM ───────────────────────────────────────────
        // High (61-100): needs calm and emotional regulation
        tasks.add(t("Neuroticism", 61, 100, "high", 1, "Do 5 minutes of box breathing right now: inhale 4s, hold 4s, exhale 4s, hold 4s. Repeat."));
        tasks.add(t("Neuroticism", 61, 100, "high", 2, "Write down every worry on your mind right now — then mark each one: 'can act' or 'let go'."));
        tasks.add(t("Neuroticism", 61, 100, "high", 3, "Go for a 20-minute walk outside with no phone — breathe deeply and notice your surroundings."));
        tasks.add(t("Neuroticism", 61, 100, "high", 4, "Write 3 things you are genuinely grateful for tonight before sleeping — be very specific."));
        tasks.add(t("Neuroticism", 61, 100, "high", 5, "Set your phone to Do Not Disturb for 2 hours today — no social media, no news checking."));
        tasks.add(t("Neuroticism", 61, 100, "high", 6, "Practice the 5-4-3-2-1 grounding: name 5 things you see, 4 you hear, 3 you can touch."));
        tasks.add(t("Neuroticism", 61, 100, "high", 7, "Exercise for 20 minutes today — even a walk counts. Physical movement reduces anxiety."));
        tasks.add(t("Neuroticism", 61, 100, "high", 8, "Write a 'brain dump' — write everything on your mind for 10 minutes without stopping."));
        tasks.add(t("Neuroticism", 61, 100, "high", 9, "When a stressful thought appears, ask: 'Is this definitely true? What is the evidence?'"));
        tasks.add(t("Neuroticism", 61, 100, "high", 10, "Set a fixed sleep time tonight and a wake time — consistent sleep dramatically reduces anxiety."));

        // Moderate Neuroticism (41-60)
        tasks.add(t("Neuroticism", 41, 60, "moderate", 1, "Start your morning with 5 minutes of calm breathing before checking your phone."));
        tasks.add(t("Neuroticism", 41, 60, "moderate", 2, "Identify one thing causing background stress today and take one concrete step to reduce it."));
        tasks.add(t("Neuroticism", 41, 60, "moderate", 3, "Write down 3 things that went well today, no matter how small, before you sleep."));
        tasks.add(t("Neuroticism", 41, 60, "moderate", 4, "Limit news and social media to 30 minutes total today — protect your mental peace."));
        tasks.add(t("Neuroticism", 41, 60, "moderate", 5, "Do something that relaxes you intentionally today — a warm shower, music, reading, a walk."));
        tasks.add(t("Neuroticism", 41, 60, "moderate", 6, "Notice one moment today where you overreacted — what triggered it? Write it down to understand it."));
        tasks.add(t("Neuroticism", 41, 60, "moderate", 7, "Talk to someone you trust about something that has been on your mind — release the mental weight."));
        tasks.add(t("Neuroticism", 41, 60, "moderate", 8, "Practice saying 'I cannot control everything' today — let go of one thing you have been overthinking."));
        tasks.add(t("Neuroticism", 41, 60, "moderate", 9, "Take 3 mindful deep breaths before responding to any stressful message or situation today."));
        tasks.add(t("Neuroticism", 41, 60, "moderate", 10, "Exercise for 15 minutes today — it is the fastest proven way to reduce cortisol (stress hormone)."));

        taskRepo.saveAll(tasks);
        System.out.println("✅ Personality tasks seeded into database: " + tasks.size() + " tasks");
    }

    private PersonalityTask t(String trait, int min, int max, String level, int num, String text) {
        PersonalityTask pt = new PersonalityTask();
        pt.setTrait(trait);
        pt.setMinScore(min);
        pt.setMaxScore(max);
        pt.setLevel(level);
        pt.setTaskNumber(num);
        pt.setTaskText(text);
        return pt;
    }
}
