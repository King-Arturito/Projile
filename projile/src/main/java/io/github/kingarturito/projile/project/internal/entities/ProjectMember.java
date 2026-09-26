package io.github.kingarturito.projile.project.internal.entities;

import io.github.kingarturito.projile.project.ProjectId;
import io.github.kingarturito.projile.project.internal.enums.ProjectRole;
import io.github.kingarturito.projile.project.internal.valueobjects.MemberId;
import io.github.kingarturito.projile.user.UserId;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;


@Entity
@Table(name = "project_member", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class ProjectMember {

    @EmbeddedId
    @EqualsAndHashCode.Include
    @AttributeOverride(
            name = "memberId",
            column = @Column(name = "member_id", nullable = false, columnDefinition = "uuid")
    )
    private MemberId memberId = MemberId.newId();


    @Embedded
    @AttributeOverride(
            name = "projectId",
            column = @Column(name = "project_id", nullable = false, columnDefinition = "uuid")
    )
    private ProjectId projectId;

    @Embedded
    @AttributeOverride(
            name = "userId",
            column = @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    )
    private UserId userId;

    @Column(name = "project_role")
    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Embedded
    @AttributeOverride(
            name = "memberId",
            column = @Column(name = "invited_by", nullable = false, columnDefinition = "uuid")
    )
    private MemberId invitedBy;

    private ProjectMember(ProjectId projectId, UserId userId, ProjectRole projectRole) {
        this.projectId = projectId;
        this.userId = userId;
        this.projectRole = projectRole;
    }

    public static ProjectMember addMember(ProjectId projectId, UserId userId, ProjectRole projectRole, MemberId invitedBy, Clock clock) {
        ProjectMember projectMember = new ProjectMember(projectId, userId, projectRole);
        projectMember.joinedAt = Instant.now(clock);
        projectMember.invitedBy = invitedBy;
        return projectMember;
    }

}
