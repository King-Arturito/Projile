package io.github.kingarturito.projile.project;


import java.util.UUID;

public interface ProjectService {


    ProjectView viewProject(UUID projectId, UUID tenantId);
}
