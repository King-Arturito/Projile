package io.github.kingarturito.projile.project;

import io.github.kingarturito.projile.shared.Description;

import java.time.Instant;

public record ProjectView(
        ProjectId projectId,
        String projectIdentifier,
        String projectName,
        Description projectDescription,
        String projectStatus,
        Instant createdAt
) {}
