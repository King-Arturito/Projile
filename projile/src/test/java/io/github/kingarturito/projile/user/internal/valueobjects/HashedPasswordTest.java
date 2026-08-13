package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidHashException;
import io.github.kingarturito.projile.user.internal.valueobjects.HashedPassword;
import org.hibernate.boot.model.source.spi.PluralAttributeMapKeyManyToAnySource;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class HashedPasswordTest {

    @ParameterizedTest
    @NullAndEmptySource
    void throwsWhenInvalid(String password) {
        assertThatThrownBy(() ->
                new HashedPassword(password)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid",
            "$argon5",
            "myPassword1234"
    })
    void throwsIfInvalidHash(String password) {
        assertThatThrownBy(() -> new HashedPassword(password)).isInstanceOf(InvalidHashException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "$argon2",
            "$argon2whatever"
    })
    void toStringReturnsRedacted(HashedPassword hashedPassword) {
        assertThat(hashedPassword.toString()).isEqualTo("[REDACTED]");
    }

    @Test
    void acceptsValidHash() {
        HashedPassword hashedPassword = new HashedPassword("$argon2awdwa");
        assertThat(hashedPassword.toString()).isEqualTo("[REDACTED]");
    }
}
