package io.github.kingarturito.projile.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.kingarturito.projile.user.*;
import io.github.kingarturito.projile.user.internal.entities.User;
import io.github.kingarturito.projile.user.internal.repositories.UserRepository;
import io.github.kingarturito.projile.user.internal.valueobjects.HashedPassword;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
import java.util.regex.Pattern;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {"spring.config.location=classpath:application-test.properties", "spring.profiles.active=test"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AuthIntegrationSupport {
    protected static final String PASSWORD = "a private integration passphrase";
    protected static final String ISSUER = "https://accounts.google.com";
    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected UserRegistrationService registration;
    @Autowired protected UserService users;
    @Autowired protected UserRepository repository;
    @Autowired protected PasswordEncoder encoder;
    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected PlatformTransactionManager transactions;
    @MockitoBean protected JavaMailSender mail;
    protected String remoteAddress;
    private static int nextAddress;

    @DynamicPropertySource static void database(DynamicPropertyRegistry properties) { TestDatabase.configure(properties); }

    @BeforeEach void isolateAccountDataAndMail() {
        // Only tables in the new test schema can be affected by this connection.
        jdbc.update("DELETE FROM email_verifications");
        jdbc.update("DELETE FROM users");
        reset(mail);
        remoteAddress = "10.10." + (++nextAddress / 250) + "." + (nextAddress % 250 + 1);
    }

    protected UserId pending(String email) { return registration.register(email); }
    protected String emailedToken() {
        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail, atLeastOnce()).send(message.capture());
        var text = message.getAllValues().getLast().getText();
        var matcher = Pattern.compile("token=([A-Za-z0-9_-]{43})").matcher(Objects.requireNonNull(text));
        if (!matcher.find()) throw new AssertionError("The delivered email did not contain a verification link.");
        return matcher.group(1);
    }
    protected UserId verified(String email) {
        pending(email); return registration.verifyEmail(emailedToken(), PASSWORD);
    }
    protected UserId complete(String email, String username) {
        UserId id = verified(email); users.chooseUsername(id.userId(), username); users.chooseDisplayName(id.userId(), "Alex Rivera"); return id;
    }
    protected UserId credentialBearingUnverified(String email) {
        return repository.saveAndFlush(User.pending(email, new HashedPassword(encoder.encode(PASSWORD)))).getUserId();
    }
    protected void ageToken(UserId userId, int seconds) {
        jdbc.update("UPDATE email_verifications SET sent_at=now()-(? * interval '1 second') WHERE user_id=?", seconds, userId.userId());
    }
    protected String body(Object value) throws Exception { return json.writeValueAsString(value); }
    protected <T> T transaction(java.util.function.Supplier<T> action) {
        return new TransactionTemplate(transactions).execute(status -> action.get());
    }
}
