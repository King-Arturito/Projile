package io.github.kingarturito.projile.user.repositories;

import io.github.kingarturito.projile.support.AuthIntegrationSupport;
import io.github.kingarturito.projile.user.internal.entities.User;
import io.github.kingarturito.projile.user.internal.valueobjects.Username;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIT extends AuthIntegrationSupport {

    @Test
    void embeddedIdAndNullableCredentialConvertersRoundTripThroughPostgres() {
        var pending = repository.saveAndFlush(User.pendingEmail("pending-repository@example.com"));
        var restored = repository.findById(pending.getUserId()).orElseThrow();

        assertThat(restored.getUserId()).isEqualTo(pending.getUserId());
        assertThat(restored.getUsername()).isNull();
        assertThat(restored.getPassword()).isNull();

        var oidcId = registration.registerOidc("oidc-repository@example.com", ISSUER, "repository-sub");

        assertThat(repository.findByOidcIssuerAndOidcSubject(ISSUER, "repository-sub")).isPresent();
        assertThat(repository.findById(oidcId).orElseThrow().getPassword()).isNull();
    }

    @Test
    void lookupsAndAvailabilityAgreeOnCaseInsensitiveNamesAndEmails() {
        var id = complete("repository@example.com", "Repository_User");

        assertThat(repository.findByUsername("REPOSITORY_USER").orElseThrow().getUserId()).isEqualTo(id);
        assertThat(repository.findByUserEmail("REPOSITORY@EXAMPLE.COM").orElseThrow().getUserId()).isEqualTo(id);
        assertThat(repository.existsByUsername("repository_user")).isTrue();
        assertThat(repository.existsByUsername(new Username("REPOSITORY_USER"))).isTrue();
        assertThat(repository.existsByUserEmail("Repository@Example.com")).isTrue();
    }

    @Test
    void displayNamesAreNotUniqueIdentities() {
        var first = complete("first-repository@example.com", "first_repository");
        var second = complete("second-repository@example.com", "second_repository");

        assertThat(users.findById(first.userId()).displayName())
                .isEqualTo(users.findById(second.userId()).displayName());
        assertThat(repository.count()).isEqualTo(2);
    }
}