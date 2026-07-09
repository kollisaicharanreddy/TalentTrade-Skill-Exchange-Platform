package com.talenttrade.config;

import com.talenttrade.entity.Skill;
import com.talenttrade.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SkillRepository skillRepository;
    private final com.talenttrade.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${admin.email:saicharanreddykolli@gmail.com}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        log.info("Checking database for default popular skills seeding...");
        seedSkills();
        bootstrapAdminUser();
    }

    private void bootstrapAdminUser() {
        if (adminEmail == null || adminEmail.trim().isEmpty()) {
            return;
        }
        if (!userRepository.existsByEmail(adminEmail)) {
            String defaultUsername = adminEmail.split("@")[0];
            com.talenttrade.entity.User admin = com.talenttrade.entity.User.builder()
                    .fullName("Default Administrator")
                    .username(defaultUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode("12345678"))
                    .emailVerified(true)
                    .enabled(true)
                    .role(com.talenttrade.entity.Role.ADMIN)
                    .provider(com.talenttrade.entity.AuthProvider.LOCAL)
                    .build();
            userRepository.save(admin);
            log.info("[AUDIT] Bootstrapped default administrator account: {} with username: {}", adminEmail,
                    defaultUsername);
        } else {
            com.talenttrade.entity.User admin = userRepository.findByEmail(adminEmail).orElse(null);
            if (admin != null && admin.getRole() != com.talenttrade.entity.Role.ADMIN) {
                admin.setRole(com.talenttrade.entity.Role.ADMIN);
                userRepository.save(admin);
                log.info("[AUDIT] Automatically upgraded existing account matching ADMIN_EMAIL to ADMIN: {}",
                        adminEmail);
            }
        }
    }

    private void seedSkills() {
        List<Skill> defaultSkills = Arrays.asList(
                new Skill(null, "Java", "Programming", "Core Java, OOP, Streams, Concurrency"),
                new Skill(null, "Python", "Programming", "Python scripting, data analysis, Django, FastAPI"),
                new Skill(null, "JavaScript", "Programming", "Web development, DOM manipulation, ES6+, async coding"),
                new Skill(null, "TypeScript", "Programming", "Static typing for modern JavaScript/React applications"),
                new Skill(null, "React", "Programming", "Frontend UI library, hooks, state management, SPA components"),
                new Skill(null, "Spring Boot", "Programming", "Backend Java microservices framework, REST, JPA"),
                new Skill(null, "Node.js", "Programming", "Server-side JavaScript runtime environments, Express"),
                new Skill(null, "SQL", "Databases", "Relational database querying, schema design, index optimization"),
                new Skill(null, "UI/UX Design", "Design",
                        "User interface and user experience wireframing, Figma prototyping"),
                new Skill(null, "Graphic Design", "Design",
                        "Visual communication, branding, typography, Illustrator/Photoshop"),
                new Skill(null, "Public Speaking", "Business",
                        "Presentation skills, confidence, body language, storytelling"),
                new Skill(null, "Negotiation", "Business",
                        "Commercial negotiation, conflict resolution, active listening"),
                new Skill(null, "Project Management", "Business",
                        "Agile methodologies, Scrum framework, timeline planning"),
                new Skill(null, "Spanish", "Language", "Conversational Spanish, vocabulary, writing, grammar"),
                new Skill(null, "English", "Language", "Professional English, vocabulary, communication, accents"),
                new Skill(null, "Photography", "Arts",
                        "Camera operations, composition, lighting, Lightroom/Photoshop editing"),
                new Skill(null, "Machine Learning", "Data Science",
                        "Supervised/unsupervised learning models, Scikit-learn, Pandas"),
                new Skill(null, "Financial Modeling", "Finance",
                        "Excel forecasting, spreadsheet modeling, valuation models"));

        int count = 0;
        for (Skill skill : defaultSkills) {
            if (!skillRepository.existsByNameIgnoreCase(skill.getName())) {
                skillRepository.save(skill);
                log.info("Seeded default registry skill: {} ({})", skill.getName(), skill.getCategory());
                count++;
            }
        }

        if (count > 0) {
            log.info("Database seeding completed. Seeded {} new skills.", count);
        } else {
            log.info("No new skills needed to seed. Universal registry is up to date.");
        }
    }
}
