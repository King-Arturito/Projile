package io.github.kingarturito.projile.project.internal;

import io.github.kingarturito.projile.project.ProjectId;
import io.github.kingarturito.projile.project.ProjectService;
import io.github.kingarturito.projile.project.ProjectView;
import io.github.kingarturito.projile.project.exceptions.ProjectNotFoundException;
import io.github.kingarturito.projile.project.internal.repositories.ProjectRepository;
import io.github.kingarturito.projile.tenant.TenantId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    public ProjectView viewProject(UUID projectId, UUID tenantId) {

        ProjectView projectView = projectRepository
                .findByProjectIdAndTenantId(
                        new ProjectId(projectId),
                        new TenantId(tenantId)
                )
                .orElseThrow(() -> new ProjectNotFoundException("Project not found"));
        return projectView;
    }
}
