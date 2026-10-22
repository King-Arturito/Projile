package io.github.kingarturito.projile.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import java.time.Duration;
import java.util.concurrent.atomic.*;
import static org.assertj.core.api.Assertions.*;

class LoginAttemptFilterTest {
    private final AtomicLong time = new AtomicLong();
    private final LoginAttemptFilter filter = new LoginAttemptFilter(time::get);
    private final AtomicInteger forwarded = new AtomicInteger();
    private MockHttpServletResponse login(String username, String ip) throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setServletPath("/api/auth/login"); request.setRemoteAddr(ip);
        request.addParameter("username", username);
        request.addHeader("X-Forwarded-For", "203.0.113." + ((username.hashCode() & 0x7fffffff) % 250 + 1));
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> forwarded.incrementAndGet());
        return response;
    }
    @Test void limitsNormalizedAccountsAcrossDifferentAddressesAndResetsWithoutSleeping() throws Exception {
        for (int i=0; i<10; i++) assertThat(login(i%2==0 ? " ALEX_USER " : "alex_user", "ip"+i).getStatus()).isEqualTo(200);
        var blocked = login("alex_user", "another-ip");
        assertThat(blocked.getStatus()).isEqualTo(429); assertThat(blocked.getHeader("Retry-After")).isEqualTo("60");
        assertThat(forwarded).hasValue(10);
        time.addAndGet(Duration.ofMinutes(1).toNanos());
        assertThat(login("alex_user", "another-ip").getStatus()).isEqualTo(200);
    }
    @Test void limitsAnAddressEvenWhenAccountNamesChange() throws Exception {
        for (int i=0; i<100; i++) login("user"+i, "one-ip");
        assertThat(login("fresh-user", "one-ip").getStatus()).isEqualTo(429);
        assertThat(login("fresh-user", "different-ip").getStatus()).isEqualTo(200);
    }
    @Test void skipsOtherRequestsAndRejectsOversizedIdentifiers() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/auth/session");
        filter.doFilter(request, new MockHttpServletResponse(), (req,res) -> forwarded.incrementAndGet());
        assertThat(forwarded).hasValue(1);
        assertThat(login("a".repeat(255), "ip").getStatus()).isEqualTo(429);
    }
    @Test void storageRemainsUsableAfterMoreThanItsCapacityOfIdentities() throws Exception {
        for (int i=0; i<10100; i++) login("user"+i, "ip"+i);
        assertThat(login("new-name", "new-ip").getStatus()).isEqualTo(200);
    }
}
