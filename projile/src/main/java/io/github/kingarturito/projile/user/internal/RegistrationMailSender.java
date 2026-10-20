package io.github.kingarturito.projile.user.internal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RegistrationMailSender {
    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String from;

    public RegistrationMailSender(JavaMailSender mailSender,
            @Value("${app.auth.frontend-url:http://localhost:4200}") String frontendUrl,
            @Value("${app.auth.mail-from:noreply@projile.local}") String from) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
        this.from = from;
    }

    public void sendVerification(String email, String token) {
        String url = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/register/verify").queryParam("token", token).build().toUriString();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Verify your Projile email");
        message.setText("Welcome to Projile.\n\nOpen this link to verify your email and create your password:\n"
                + url + "\n\nThis link expires in 30 minutes and can only be used once. "
                + "If you did not request this, ignore this email.\n");
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            // Never expose mail transport configuration or the verification link in an HTTP response.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Email delivery is temporarily unavailable.");
        }
    }
}
