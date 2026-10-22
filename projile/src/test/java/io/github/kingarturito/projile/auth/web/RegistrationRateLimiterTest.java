package io.github.kingarturito.projile.auth.web;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.*;

class RegistrationRateLimiterTest {
    private final AtomicLong time = new AtomicLong();
    private final RegistrationRateLimiter limiter = new RegistrationRateLimiter(time::get);
    @Test void limitsIndependentlyByNamespaceAndResetsOnTheExactBoundary() {
        limiter.check("email", "alex@example.com", 1, Duration.ofMinutes(1));
        assertThatThrownBy(() -> limiter.check("email", "alex@example.com", 1, Duration.ofMinutes(1)))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> assertThat(e.getStatusCode().value()).isEqualTo(429));
        assertThatCode(() -> limiter.check("token", "alex@example.com", 1, Duration.ofMinutes(1))).doesNotThrowAnyException();
        time.set(60000);
        assertThatCode(() -> limiter.check("email", "alex@example.com", 1, Duration.ofMinutes(1))).doesNotThrowAnyException();
    }
    @Test void boundsMemoryWithoutDroppingAnActiveIdentityLimit() {
        for (int i=0; i<10000; i++) limiter.check("email", "email"+i, 1, Duration.ofMinutes(1));
        assertThatThrownBy(() -> limiter.check("email", "new", 1, Duration.ofMinutes(1))).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> limiter.check("email", "email0", 1, Duration.ofMinutes(1))).isInstanceOf(ResponseStatusException.class);
        time.set(60000);
        assertThatCode(() -> limiter.check("email", "new", 1, Duration.ofMinutes(1))).doesNotThrowAnyException();
    }
}
