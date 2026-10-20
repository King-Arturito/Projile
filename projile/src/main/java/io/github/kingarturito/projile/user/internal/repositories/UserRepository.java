package io.github.kingarturito.projile.user.internal.repositories;

import io.github.kingarturito.projile.shared.UsernameUniquenessChecker;
import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.internal.entities.User;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UserId>, UsernameUniquenessChecker {
    boolean existsByUserEmailIgnoreCase(String email);
    Optional<User> findByUserEmailIgnoreCase(String email);

    default boolean existsByUserEmail(String email) {
        return existsByUserEmailIgnoreCase(email);
    }

    default Optional<User> findByUserEmail(String email) {
        return findByUserEmailIgnoreCase(email);
    }

    @Override
    @Query(value = "SELECT EXISTS (SELECT 1 FROM users WHERE lower(username) = lower(:username))", nativeQuery = true)
    boolean existsByUsername(@Param("username") String username);

    default boolean existsByUsername(Username username) {
        return existsByUsername(username.value());
    }

    @Query(value = "SELECT * FROM users WHERE lower(username) = lower(:username)", nativeQuery = true)
    Optional<User> findByUsername(@Param("username") String username);

    @Query("select u.userId from User u where lower(u.userEmail) = lower(:email)")
    Optional<UserId> findUserIdByEmail(@Param("email") String email);

    Optional<User> findByOidcIssuerAndOidcSubject(String issuer, String subject);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> lockById(@Param("userId") UserId userId);
}
