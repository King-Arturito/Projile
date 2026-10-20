package io.github.kingarturito.projile.user.internal.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** Only a SHA-256 digest is persisted; the bearer token exists only in the email. */
@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class EmailVerification {
    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    public EmailVerification(UUID userId, String tokenHash, Instant now) {
        this.userId = userId;
        renew(tokenHash, now);
    }

    public void renew(String tokenHash, Instant now) {
        this.tokenHash = tokenHash;
        this.sentAt = now;
        this.expiresAt = now.plusSeconds(1800);
    }
}
