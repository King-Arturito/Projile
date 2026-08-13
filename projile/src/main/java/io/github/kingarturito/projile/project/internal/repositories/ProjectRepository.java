package io.github.kingarturito.projile.project.internal.repositories;

import io.github.kingarturito.projile.project.ProjectId;
import io.github.kingarturito.projile.project.ProjectView;
import io.github.kingarturito.projile.project.internal.entities.Project;
import io.github.kingarturito.projile.tenant.TenantId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, ProjectId> {

    Optional<ProjectView> findByProjectIdAndTenantId(ProjectId projectId, TenantId tenantId);

    List<Project> findAllByTenantId(TenantId tenantId, Pageable pageable);
}
