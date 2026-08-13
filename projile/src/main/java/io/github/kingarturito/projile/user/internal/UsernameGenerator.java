package io.github.kingarturito.projile.user.internal;

import io.github.kingarturito.projile.shared.UsernameUniquenessChecker;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Component
public class UsernameGenerator {

    private static final int MAX_ATTEMPTS = 10;
    private static final int MAX_LENGTH = 32;

    private final UsernameUniquenessChecker usernameUniquenessChecker;

    public Username generateUsername(String email) {
        String base = padToMinLength(sanitize(email.split("@")[0]));

        String candidate = truncate(base, "");
        if (!usernameUniquenessChecker.existsByUsername(candidate)) {
            return new Username(candidate);
        }

        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String suffix = String.format("%04d",
                    ThreadLocalRandom.current().nextInt(10_000));

            candidate = truncate(base, suffix) + suffix;

            if (!usernameUniquenessChecker.existsByUsername(candidate)) {
                return new Username(candidate);
            }
        }

        throw new IllegalStateException("Unable to generate a unique username.");
    }

    private String sanitize(String raw) {
        String cleaned = raw.replaceAll("[^A-Za-z0-9_]", "");

        if (cleaned.isEmpty() || !Character.isLetter(cleaned.charAt(0))) {
            cleaned = "user" + cleaned;
        }

        return cleaned;
    }

    private String padToMinLength(String value) {
        StringBuilder sb = new StringBuilder(value);

        while (sb.length() < 6) {
            sb.append('0');
        }

        return sb.toString();
    }

    private String truncate(String base, String suffix) {
        int maxBaseLength = MAX_LENGTH - suffix.length();

        if (base.length() <= maxBaseLength) {
            return base;
        }

        return base.substring(0, maxBaseLength);
    }
}