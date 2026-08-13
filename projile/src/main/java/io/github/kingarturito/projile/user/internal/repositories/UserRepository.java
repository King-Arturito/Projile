package io.github.kingarturito.projile.user.internal.repositories;

import io.github.kingarturito.projile.shared.UsernameUniquenessChecker;
import io.github.kingarturito.projile.user.internal.entities.User;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, UsernameUniquenessChecker {
    boolean existsByUserEmail(String email);
    boolean existsByUsername(Username username);
    Optional<User> findByUserEmail(String email);

    Optional<User> findByUsername(Username username);
}
