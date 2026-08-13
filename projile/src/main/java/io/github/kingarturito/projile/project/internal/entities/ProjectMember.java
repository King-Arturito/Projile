package io.github.kingarturito.projile.project.internal.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.kingarturito.projile.project.internal.enums.ProjectRole;
import io.github.kingarturito.projile.project.internal.valueobjects.MemberId;
import io.github.kingarturito.projile.project.internal.valueobjects.converters.MemberIdConverter;
import io.github.kingarturito.projile.project.ProjectId;
import io.github.kingarturito.projile.project.ProjectIdConverter;
import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.converters.UserIdConverter;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Clock;
import java.time.Instant;


@Entity
@Table(name = "project_member", uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class ProjectMember {
    @Id
    @Column(name = "member_id")
    @Convert(converter = MemberIdConverter.class)
    @EqualsAndHashCode.Include
    @JdbcTypeCode(SqlTypes.UUID)
    private MemberId memberId = new MemberId(UuidCreator.getTimeOrderedEpoch());

    @Column(name = "project_id", nullable = false)
    @Convert(converter = ProjectIdConverter.class)
    private ProjectId projectId;

    @Column(name = "user_id", nullable = false)
    @Convert(converter = UserIdConverter.class)
    private UserId userId;

    @Column(name = "project_role")
    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "invited_by")
    @Convert(converter = MemberIdConverter.class)
    private MemberId invitedBy;

    @Column(name = "invitation_message", nullable = true, length = 255)
    private String invitationMessage;

    private ProjectMember(ProjectId projectId, UserId userId, ProjectRole projectRole) {
        this.projectId = projectId;
        this.userId = userId;
        this.projectRole = projectRole;
    }

    public static ProjectMember addMember(ProjectId projectId, UserId userId, ProjectRole projectRole, MemberId invitedBy, String invitationMessage, Clock clock) {
        ProjectMember projectMember = new ProjectMember(projectId, userId, projectRole);
        projectMember.joinedAt = Instant.now(clock);
        projectMember.invitationMessage =  invitationMessage;
        projectMember.invitedBy = invitedBy;
        return projectMember;
    }

}
