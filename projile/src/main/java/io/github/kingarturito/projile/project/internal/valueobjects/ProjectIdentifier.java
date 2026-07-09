package io.github.kingarturito.projile.project.internal.valueobjects;

import io.github.kingarturito.projile.project.exceptions.InvalidProjectIdentifierException;

import java.util.Set;

public record ProjectIdentifier(String value) {
    private static final Set<String> RESERVED = Set.of("ADMIN", "SYSTEM");
    public ProjectIdentifier {
        validateLength(value);
        validateCharset(value);
        validateName(value);
    }

    private static void validateLength(String projectIdentifier) {
        if (projectIdentifier.length() > 10 ||  projectIdentifier.length() < 4) {
            throw new InvalidProjectIdentifierException("Project identifier must be between 4 and 10 characters.");
        }
    }

    private static void validateName(String projectIdentifier) {
        if (RESERVED.contains(projectIdentifier)) {
            throw new InvalidProjectIdentifierException("Project Identifier contains reserved characters.");
        }
    }

    private static void validateCharset(String value) {
        if (!value.matches("[A-Z0-9]+")) {
            throw new InvalidProjectIdentifierException("Project identifier must be uppercase alphanumeric.");
        }
    }
}
