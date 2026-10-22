package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidPasswordException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class PasswordTest {
    @ParameterizedTest
    @ValueSource(strings = {"alllowercasepassphrase", "ALLUPPERCASEPASSPHRASE", "123456789012345", "a password with spaces", "  a private passphrase  "})
    void allowsPassphrasesWithoutCompositionRequirements(String raw) {
        assertThat(new Password(raw).value()).isEqualTo(raw);
    }
    @Test void countsUnicodeCodePointsRatherThanUtf16Units() {
        assertThat(new Password("🌱".repeat(15)).value()).isEqualTo("🌱".repeat(15));
        assertThat(new Password("🌱".repeat(128)).value()).hasSize(256);
        assertThatThrownBy(() -> new Password("🌱".repeat(14))).isInstanceOf(InvalidPasswordException.class);
        assertThatThrownBy(() -> new Password("🌱".repeat(129))).isInstanceOf(InvalidPasswordException.class);
    }
    @Test void enforcesBothExactLengthBoundaries() {
        assertThatCode(() -> new Password("a".repeat(15))).doesNotThrowAnyException();
        assertThatCode(() -> new Password("a".repeat(128))).doesNotThrowAnyException();
        assertThatThrownBy(() -> new Password("a".repeat(14))).isInstanceOf(InvalidPasswordException.class);
        assertThatThrownBy(() -> new Password("a".repeat(129))).isInstanceOf(InvalidPasswordException.class);
    }
    @ParameterizedTest @NullAndEmptySource
    @ValueSource(strings = {"too short", "a long\ninvalid password", "a long\tinvalid password", "a long\u0000invalid password", "a long\u2028invalid password", "a long\u2029invalid password", "a long\uD800invalid password", "a long\uDC00invalid password"})
    void rejectsInvalidPasswords(String raw) {
        assertThatThrownBy(() -> new Password(raw)).isInstanceOf(InvalidPasswordException.class);
    }
    @Test void neverIncludesThePasswordInItsStringRepresentation() {
        assertThat(new Password("private passphrase").toString()).isEqualTo("[REDACTED]");
    }
}
