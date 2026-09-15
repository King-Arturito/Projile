package io.github.kingarturito.projile.user.internal.entities;

import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.converters.HashedPasswordConverter;
import io.github.kingarturito.projile.user.converters.UsernameConverter;
import io.github.kingarturito.projile.user.internal.valueobjects.HashedPassword;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
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

    @Column(name = "username", unique = true, nullable = false)
    @Convert(converter = UsernameConverter.class)
    private Username username;

    @Column(name = "user_email", unique = true)
    private String userEmail;

    @Convert(converter = HashedPasswordConverter.class)
    @Column(name = "hashed_password", nullable = false)
    private HashedPassword password;

    private User(String email, HashedPassword password, Username username) {
        this.userEmail = email;
        this.password = password;
        this.username = username;
    }

    public static User register(String email, HashedPassword password, Username username) {
        return new User(email, password, username);
    }
}
