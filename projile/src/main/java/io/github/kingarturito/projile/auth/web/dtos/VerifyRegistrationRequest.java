package io.github.kingarturito.projile.auth.web.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyRegistrationRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}") String token,
        @NotBlank @Size(max = 256) String password) {
    @Override
    public String toString() { return "VerifyRegistrationRequest[REDACTED]"; }
}
