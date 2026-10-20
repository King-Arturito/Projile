package io.github.kingarturito.projile.user.internal.entities;

import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import io.github.kingarturito.projile.shared.exceptions.ErrorCode;
import io.github.kingarturito.projile.user.converters.DisplayNameConverter;
import io.github.kingarturito.projile.user.converters.HashedPasswordConverter;
import io.github.kingarturito.projile.user.converters.UsernameConverter;
import io.github.kingarturito.projile.user.internal.valueobjects.HashedPassword;
import io.github.kingarturito.projile.user.internal.valueobjects.DisplayName;
import io.github.kingarturito.projile.user.internal.valueobjects.Email;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@Table(name = "users", uniqueConstraints = @UniqueConstraint(
        name = "users_oidc_identity_unique", columnNames = {"oidc_issuer", "oidc_subject"}))
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @EmbeddedId
    @EqualsAndHashCode.Include
    @AttributeOverride(
            name = "userId",
            column = @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    )
    private UserId userId = UserId.newId();

    @Column(name = "username", unique = true)
    @Convert(converter = UsernameConverter.class)
    private Username username;

    @Column(name = "user_email", unique = true, nullable = false)
    private String userEmail;

    @Convert(converter = HashedPasswordConverter.class)
    @Column(name = "hashed_password")
    private HashedPassword password;

    @Column(name = "display_name", length = 80)
    @Convert(converter = DisplayNameConverter.class)
    private DisplayName displayName;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "oidc_issuer", length = 512)
    private String oidcIssuer;

    @Column(name = "oidc_subject")
    private String oidcSubject;

    private User(String email, HashedPassword password, Username username) {
        this.userEmail = Email.normalize(email);
        this.password = password;
        this.username = username;
    }

    public static User register(String email, HashedPassword password, Username username) {
        return new User(email, Objects.requireNonNull(password), Objects.requireNonNull(username));
    }

    public static User pending(String email, HashedPassword password) {
        return new User(email, Objects.requireNonNull(password), null);
    }

    /** Registration reserves the address; credentials are chosen only after mailbox ownership is proved. */
    public static User pendingEmail(String email) {
        return new User(email, null, null);
    }

    /** Only call after the identity provider has validated its signed, verified email claim. */
    public static User pendingOidc(String email, String issuer, String subject) {
        if (issuer == null || issuer.isBlank() || issuer.length() > 512
                || subject == null || subject.isBlank() || subject.length() > 255) {
            throw new IllegalArgumentException("A valid OpenID Connect issuer and subject are required.");
        }
        User user = new User(email, null, null);
        user.oidcIssuer = issuer;
        user.oidcSubject = subject;
        user.emailVerified = true;
        return user;
    }

    /** Claiming a credentialless pending email is safe only after trusted provider mailbox proof. */
    public void completeOidcRegistration(String issuer, String subject) {
        if (emailVerified || password != null || username != null || oidcSubject != null) {
            throw new BadRequestException(ErrorCode.ONBOARDING_SEQUENCE_INVALID);
        }
        User validatedIdentity = pendingOidc(userEmail, issuer, subject);
        this.oidcIssuer = validatedIdentity.oidcIssuer;
        this.oidcSubject = validatedIdentity.oidcSubject;
        this.emailVerified = true;
    }

    public void verifyEmail() {
        if (password == null && oidcSubject == null) {
            throw new BadRequestException(ErrorCode.ONBOARDING_SEQUENCE_INVALID,
                    "Choose a password while verifying your email.");
        }
        this.emailVerified = true;
    }

    /** Call only while consuming the user's valid, unexpired verification token. */
    public void completeEmailVerification(HashedPassword password) {
        if (emailVerified) {
            throw new BadRequestException(ErrorCode.ONBOARDING_SEQUENCE_INVALID,
                    "Email has already been verified.");
        }
        this.password = Objects.requireNonNull(password, "password is required");
        this.emailVerified = true;
    }

    public void chooseUsername(Username username) {
        requireVerifiedEmail();
        Objects.requireNonNull(username, "username is required");
        if (this.username != null && !this.username.equals(username)) {
            throw new BadRequestException(ErrorCode.ONBOARDING_SEQUENCE_INVALID,
                    "A username has already been chosen.");
        }
        this.username = username;
    }

    public void chooseDisplayName(DisplayName displayName) {
        requireVerifiedEmail();
        if (username == null) {
            throw new BadRequestException(ErrorCode.ONBOARDING_SEQUENCE_INVALID);
        }
        this.displayName = Objects.requireNonNull(displayName, "display name is required");
    }

    public boolean isProfileComplete() {
        return emailVerified && username != null && displayName != null;
    }

    private void requireVerifiedEmail() {
        if (!emailVerified) {
            throw new BadRequestException(ErrorCode.ONBOARDING_SEQUENCE_INVALID,
                    "Verify your email before choosing your profile details.");
        }
    }
}
