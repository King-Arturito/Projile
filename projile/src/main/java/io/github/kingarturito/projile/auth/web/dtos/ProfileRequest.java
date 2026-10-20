package io.github.kingarturito.projile.auth.web.dtos;

import jakarta.validation.constraints.Size;

/** Each submission advances exactly one onboarding step; values are validated in the domain. */
public record ProfileRequest(@Size(max = 32) String username, @Size(max = 160) String displayName) {}
