package io.github.kingarturito.projile.auth.web;

import io.github.kingarturito.projile.auth.security.UserPrincipal;
import io.github.kingarturito.projile.auth.web.dtos.ProfileRequest;
import io.github.kingarturito.projile.auth.web.dtos.RegisterRequest;
import io.github.kingarturito.projile.auth.web.dtos.VerificationEmailRequest;
import io.github.kingarturito.projile.auth.web.dtos.VerifyRegistrationRequest;
import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.UserRegistrationService;
import io.github.kingarturito.projile.user.UserService;
import io.github.kingarturito.projile.user.dtos.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRegistrationService registrationService;
    private final UserService userService;
    private final RegistrationRateLimiter rateLimiter;
    private final SecurityContextRepository contextRepository;
    private final SessionAuthenticationStrategy sessionStrategy;
    private final ObjectProvider<ClientRegistrationRepository> registrations;

    @GetMapping("/session")
    public SessionResponse session(Authentication authentication, CsrfToken csrfToken,
            HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        csrfToken.getToken(); // Materialize the deferred CSRF cookie, including after login/logout.
        UserDto user = authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal
                ? userService.findById(principal.getUserId()) : null;
        ClientRegistrationRepository providers = registrations.getIfAvailable();
        return new SessionResponse(user != null, user,
                providers != null && providers.findByRegistrationId("google") != null);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest body, HttpServletRequest request) {
        limitEmail(request, body.email());
        try {
            registrationService.register(body.email());
        } catch (DataIntegrityViolationException exception) {
            // A concurrent registration can win the unique email constraint; do not enumerate accounts.
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/verification")
    public ResponseEntity<Void> resend(@Valid @RequestBody VerificationEmailRequest body, HttpServletRequest request) {
        limitEmail(request, body.email());
        registrationService.resendVerification(body.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verify(@Valid @RequestBody VerifyRegistrationRequest body,
            HttpServletRequest request, HttpServletResponse response) {
        rateLimiter.check("verify-ip", request.getRemoteAddr(), 20, Duration.ofMinutes(1));
        rateLimiter.check("verify-token", body.token(), 5, Duration.ofMinutes(15));
        UserId userId = registrationService.verifyEmail(body.token(), body.password());
        saveAuthentication(userId.userId(), request, response, true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/profile")
    public UserDto profile(@Valid @RequestBody ProfileRequest body, Authentication authentication,
            HttpServletRequest request, HttpServletResponse response) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if ((body.username() == null) == (body.displayName() == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Submit one profile field at a time.");
        }
        UserDto user = body.username() != null
                ? userService.chooseUsername(principal.getUserId(), body.username())
                : userService.chooseDisplayName(principal.getUserId(), body.displayName());
        saveAuthentication(principal.getUserId(), request, response, false);
        response.setHeader("Cache-Control", "no-store");
        return user;
    }

    private void saveAuthentication(UUID userId, HttpServletRequest request, HttpServletResponse response,
            boolean newLogin) {
        UserPrincipal principal = new UserPrincipal(userService.loadAuthenticatedUserById(userId));
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());
        if (newLogin) sessionStrategy.onAuthentication(authentication, request, response);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
    }

    private void limitEmail(HttpServletRequest request, String email) {
        rateLimiter.check("registration-ip", request.getRemoteAddr(), 30, Duration.ofHours(1));
        rateLimiter.check("registration-email", email.strip().toLowerCase(Locale.ROOT), 5, Duration.ofHours(1));
    }

    public record SessionResponse(boolean authenticated, UserDto user, boolean googleEnabled) {}
}
