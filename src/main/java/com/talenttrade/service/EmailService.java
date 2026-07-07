package com.talenttrade.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendVerificationEmail(String toEmail, String name, String verificationUrl) {
        log.info("Sending verification email to {}", toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("verificationUrl", verificationUrl);

            String html = templateEngine.process("verification-email", context);

            helper.setTo(toEmail);
            helper.setSubject("Verify Your Email - TalentTrade");
            helper.setText(html, true);
            helper.setFrom("no-reply@talenttrade.com");

            mailSender.send(message);
            log.info("Verification email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
