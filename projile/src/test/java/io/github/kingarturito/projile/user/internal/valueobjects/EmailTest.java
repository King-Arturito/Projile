package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.shared.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class EmailTest {
    @Test void normalizesCaseAndSurroundingWhitespace() {
        assertThat(new Email("  Alex+Tag@EXAMPLE.COM  ").value()).isEqualTo("alex+tag@example.com");
        assertThat(new Email("Alex@example.com")).isEqualTo(new Email("alex@EXAMPLE.COM"));
    }
    @Test void acceptsExactLocalPartAndAddressBoundaries() {
        String longest = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(61);
        assertThat(longest).hasSize(254);
        assertThat(new Email(longest).value()).isEqualTo(longest);
        assertThatThrownBy(() -> new Email(longest + "d")).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> new Email("a".repeat(65) + "@example.com")).isInstanceOf(BadRequestException.class);
    }
    @ParameterizedTest @NullAndEmptySource
    @ValueSource(strings = {" ", "alex", "alex@", "@example.com", "alex@localhost", "a b@example.com", ".alex@example.com", "alex.@example.com", "al..ex@example.com", "alex@-example.com", "alex@example..com", "alex\n@example.com"})
    void rejectsUnsupportedAddresses(String address) {
        assertThatThrownBy(() -> new Email(address)).isInstanceOf(BadRequestException.class);
    }
}
