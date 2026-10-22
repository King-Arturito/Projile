package io.github.kingarturito.projile.user.internal.valueobjects;

import io.github.kingarturito.projile.user.internal.exceptions.InvalidDisplayNameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class DisplayNameTest {
    @Test void acceptsNonUniqueUnicodeNamesAndInteriorSpaces() {
        assertThat(new DisplayName("  José  李  ").value()).isEqualTo("José  李");
        assertThat(new DisplayName("Alex Rivera")).isEqualTo(new DisplayName("Alex Rivera"));
    }
    @Test void checksExactCodePointLengthBoundaries() {
        assertThat(new DisplayName("名").value()).isEqualTo("名");
        assertThat(new DisplayName("🌱".repeat(80)).value()).hasSize(160);
        assertThatThrownBy(() -> new DisplayName("🌱".repeat(81))).isInstanceOf(InvalidDisplayNameException.class);
    }
    @ParameterizedTest @NullAndEmptySource
    @ValueSource(strings = {" ", "Alex\nName", "\nAlex", "Alex\t", "Alex\tName", "Alex\u0000Name", "Alex\u200BName", "Alex\u2028Name", "Alex\u2029Name", "Alex\uD800Name", "Alex\uDC00Name"})
    void rejectsBlankAndUnsupportedCharacters(String raw) {
        assertThatThrownBy(() -> new DisplayName(raw)).isInstanceOf(InvalidDisplayNameException.class);
    }
}
