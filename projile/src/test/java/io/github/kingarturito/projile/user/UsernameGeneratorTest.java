package io.github.kingarturito.projile.user;

import io.github.kingarturito.projile.shared.UsernameUniquenessChecker;
import io.github.kingarturito.projile.user.internal.UsernameGenerator;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsernameGeneratorTest {
    @Mock
    UsernameUniquenessChecker usernameUniquenessChecker;

    @InjectMocks
    UsernameGenerator usernameGenerator;

    @ParameterizedTest
    @ValueSource(strings = {
            "valid@gmail.com",
            "VeryLongEmail.arthurShouldBeInvalidIThink1231231@gmail.com",
    })
    void shouldGenerateAValidUsername(String email) {
        when(usernameUniquenessChecker.existsByUsername(anyString())).thenReturn(false);

        Username result = usernameGenerator.generateUsername(email);
        assertThat(result).isNotNull();
        assertTrue(result.value().length() >= 6);
        assertTrue(result.value().length() <= 32);
        verify(usernameUniquenessChecker).existsByUsername(anyString());
    }

    @Test
    void shouldSanitizeProperly() {
        when(usernameUniquenessChecker.existsByUsername(anyString())).thenReturn(false);
        Username user =  usernameGenerator.generateUsername("johndoe+test2@gmail.com");
        assertThat(user).isNotNull();
        assertEquals("johndoetest2", user.value());
    }
}
