package io.github.kingarturito.projile.auth.web;

import io.github.kingarturito.projile.auth.web.dtos.RegisterRequest;
import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRegistrationService userRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<Void>  register(@RequestBody RegisterRequest request) {
        UserId userId = userRegistrationService.register(request.email(), request.password());
        return ResponseEntity.created(URI.create("/api/users/" + userId.value())).build();
    }

//    @PostMapping("/login")
//    public ResponseEntity<Void> login(@RequestBody )

}
