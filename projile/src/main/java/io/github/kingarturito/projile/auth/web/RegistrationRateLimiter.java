package io.github.kingarturito.projile.auth.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/** Small single-instance limiter. Use a shared gateway/store when deploying multiple replicas. */
@Component
public class RegistrationRateLimiter {
    private static final int MAX_KEYS = 10000;
    private final Map<String, Bucket> buckets = new HashMap<>();

    public synchronized void check(String namespace, String identity, int maximum, Duration window) {
        long now = System.currentTimeMillis();
        String key = namespace + ":" + digest(identity);
        if (buckets.size() >= MAX_KEYS) {
            buckets.entrySet().removeIf(entry -> entry.getValue().until <= now);
            if (buckets.size() >= MAX_KEYS && !buckets.containsKey(key)) throw limited();
        }
        Bucket bucket = buckets.get(key);
        if (bucket == null || bucket.until <= now) {
            bucket = new Bucket(now + window.toMillis());
            buckets.put(key, bucket);
        }
        if (++bucket.count > maximum) throw limited();
    }

    private ResponseStatusException limited() {
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Please try again later.");
    }

    private String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static class Bucket {
        private final long until;
        private int count;
        private Bucket(long until) { this.until = until; }
    }
}
