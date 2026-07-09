package io.github.kingarturito.projile.project.internal.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.kingarturito.projile.project.ProjectId;
import io.github.kingarturito.projile.project.ProjectIdConverter;
import io.github.kingarturito.projile.project.internal.enums.ProjectStatus;
import io.github.kingarturito.projile.project.internal.valueobjects.ProjectIdentifier;
import io.github.kingarturito.projile.project.internal.valueobjects.converters.ProjectIdentifierConverter;
import io.github.kingarturito.projile.shared.Description;
import io.github.kingarturito.projile.shared.DescriptionConverter;
import io.github.kingarturito.projile.tenant.TenantId;
import io.github.kingarturito.projile.tenant.TenantIdConverter;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@Entity
@Table(name = "project")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class Project {

    @Id
    @Column(name = "project_id")
    @Convert(converter = ProjectIdConverter.class)
    @EqualsAndHashCode.Include
    private ProjectId projectId = new ProjectId(UuidCreator.getTimeOrderedEpoch());

    @Column(name =  "tenant_id", nullable = false)
    @Convert(converter = TenantIdConverter.class)
    private TenantId tenantId;

    @Column(name =  "project_name", nullable = false)
    private String projectName;

    @Column(name = "project_description", length = 2000)
    @Convert(converter = DescriptionConverter.class)
    private Description projectDescription;

    @Column(name = "project_identifier", unique = true, nullable = false, length = 10)
    @Convert(converter =  ProjectIdentifierConverter.class)
    private ProjectIdentifier projectIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name  = "project_status", nullable = false)
    private ProjectStatus projectStatus = ProjectStatus.IN_PROGRESS;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    private Project(String projectName, ProjectIdentifier projectIdentifier, TenantId tenantId) {
        this.projectName = projectName;
        this.projectIdentifier = projectIdentifier;
        this.tenantId = tenantId;
    }

    public static Project create(String projectName, ProjectIdentifier projectIdentifier, TenantId tenantId, Clock clock) {
        Project project = new Project(projectName, projectIdentifier, tenantId);
        project.createdAt = Instant.now(clock);
        return project;
    }

    public void rename(String projectName, Clock clock) {
        this.projectName = projectName;
        this.updatedAt = Instant.now(clock);
    }

    public void changeDescription(Description description, Clock clock) {
        this.projectDescription = description;
        this.updatedAt = Instant.now(clock);
    }

    public void archive(Clock clock) {
        this.archivedAt = Instant.now(clock);
        this.projectStatus = ProjectStatus.ARCHIVED;
    }
}
