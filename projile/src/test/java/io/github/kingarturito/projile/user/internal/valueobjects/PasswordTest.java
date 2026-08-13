package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidPasswordException;
import io.github.kingarturito.projile.user.internal.valueobjects.Password;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PasswordTest {
    @Test
    void acceptsValidPassword() {
        Password password = new Password("myPassword#129");
        assertThat(password.toString()).isEqualTo("[REDACTED]");
    }

    @ParameterizedTest()
    @ValueSource(strings = {
            "min",
            "thisStringIsWayTooLongBrotherManRejectThisPleaseINeedThisssssssssssssss12345#",
            "NoNumber#",
            "nocapital#1927",
            "ALLCAPS#12",
    })
    void throwsWhenInvalid(String value) {
        assertThatThrownBy(() -> new Password(value)).isInstanceOf(InvalidPasswordException.class);
    }
}
