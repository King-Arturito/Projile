package io.github.kingarturito.projile.user.internal;

import io.github.kingarturito.projile.user.internal.valueobjects.Password;
import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.UserRegistrationService;
import io.github.kingarturito.projile.user.internal.entities.User;
import io.github.kingarturito.projile.user.internal.exceptions.EmailAlreadyRegisteredException;
import io.github.kingarturito.projile.user.internal.repositories.UserRepository;
import io.github.kingarturito.projile.user.internal.valueobjects.HashedPassword;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsernameGenerator usernameGenerator;

    public UserId register(String email, String password) {
        if (userRepository.existsByUserEmail(email)) {
            throw new EmailAlreadyRegisteredException("Email is already in use.");
        }

        Username username = usernameGenerator.generateUsername(email);
        Password verifiedPassword = new Password(password);
        User user = User.register(email, new HashedPassword(passwordEncoder.encode(verifiedPassword.value())), username);
        userRepository.save(user);

        return user.getUserId();
    }

}
