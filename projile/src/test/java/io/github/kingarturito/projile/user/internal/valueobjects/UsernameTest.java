package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidUsernameException;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class UsernameTest {

    @Test
    void acceptsValidUsername() {
        Username username = new Username("arthur_evans");
        assertThat(username.toString()).isEqualTo("arthur_evans");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void throwsIfEmpty(String username) {
        assertThatThrownBy(() -> new Username(username)).isInstanceOf(InvalidUsernameException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "short",
            "thisUsernameIsWayTooLongAndShouldNotBeAllowed",
            "contains{}}{",
            "192arth",
    })
    void throwsIfInvalid(String username) {
        assertThatThrownBy(() -> new Username(username)).isInstanceOf(InvalidUsernameException.class);
    }
}
