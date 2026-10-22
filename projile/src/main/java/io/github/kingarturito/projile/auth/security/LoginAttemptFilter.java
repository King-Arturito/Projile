package io.github.kingarturito.projile.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.LongSupplier;

/** A bounded, single-instance throttle; production clusters should share a gateway limiter. */
final class LoginAttemptFilter extends OncePerRequestFilter {
    private static final long WINDOW_NANOS = Duration.ofMinutes(1).toNanos();
    private static final int MAX_KEYS = 10_000;
    private final Map<String, Window> windows = new LinkedHashMap<>();
    private final LongSupplier nanoClock;

    LoginAttemptFilter() { this(System::nanoTime); }

    LoginAttemptFilter(LongSupplier nanoClock) { this.nanoClock = nanoClock; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !"/api/auth/login".equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        if (username == null) {
            username = "";
        }
        if (username.length() > 254 || !allow("ip:" + request.getRemoteAddr(), 100)
                || !allow("username:" + username.trim().toLowerCase(Locale.ROOT), 10)) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Too many sign-in attempts. Try again shortly.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private synchronized boolean allow(String key, int limit) {
        long now = nanoClock.getAsLong();
        Window window = windows.get(key);
        if (window == null || now - window.startedAt >= WINDOW_NANOS) {
            if (window == null && windows.size() >= MAX_KEYS) {
                windows.remove(windows.keySet().iterator().next());
            }
            windows.put(key, new Window(now, 1));
            return true;
        }
        if (window.attempts >= limit) {
            return false;
        }
        windows.put(key, new Window(window.startedAt, window.attempts + 1));
        return true;
    }

    private record Window(long startedAt, int attempts) { }
}
