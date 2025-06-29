package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final ApplicationProperties applicationProperties;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String title = "Please Verify Your Account";
        String link = applicationProperties.getMail().getBaseUrl() + "/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("link", link);

        sendHtmlEmail(to, title, "mail/verification-email", context);
    }

    @Async
    public void sendConfirmEmail(String to, String token) {
        String title = "Please Confirm Your New Email";
        String link = applicationProperties.getMail().getBaseUrl() + "/confirm-email?token=" + token;

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("link", link);
        context.setVariable("newEmail", to);

        sendHtmlEmail(to, title, "mail/email-confirmation", context);
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String title = "Reset Your Password";
        String link = applicationProperties.getMail().getBaseUrl() + "/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("title", title);
        context.setVariable("link", link);

        sendHtmlEmail(to, title, "mail/reset-password", context);
    }


    private void sendHtmlEmail(String to, String subject, String templateName,
                               Context context) {
        try {
            String htmlBody = templateEngine.process(templateName, context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setFrom(applicationProperties.getMail().getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.addInline("logoCid", new ClassPathResource("static/images/logo.png"));
            mailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            log.warn("Email could not be sent to '{}'", to, e);
        }
    }
}
