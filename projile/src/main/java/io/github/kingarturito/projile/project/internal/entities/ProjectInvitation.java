package io.github.kingarturito.projile.project.internal.entities;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.kingarturito.projile.project.ProjectId;
import io.github.kingarturito.projile.project.ProjectIdConverter;
import io.github.kingarturito.projile.project.internal.enums.InvitationStatus;
import io.github.kingarturito.projile.project.internal.valueobjects.InvitationId;
import io.github.kingarturito.projile.project.internal.valueobjects.MemberId;
import io.github.kingarturito.projile.project.internal.valueobjects.converters.InvitationIdConverter;
import io.github.kingarturito.projile.project.internal.valueobjects.converters.MemberIdConverter;
import io.github.kingarturito.projile.user.UserId;
import io.github.kingarturito.projile.user.UserIdConverter;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "project_invitation")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
public class ProjectInvitation {

    @Id
    @Column(name = "invitation_id")
    @Convert(converter = InvitationIdConverter.class)
    @EqualsAndHashCode.Include
    private final InvitationId invitationId = new InvitationId(UuidCreator.getTimeOrderedEpoch());

    @Column(name = "project_id", nullable = false)
    @Convert(converter = ProjectIdConverter.class)
    private ProjectId projectId;

    @Column(name = "inviter_member_id", nullable = false)
    @Convert(converter = MemberIdConverter.class)
    private MemberId inviterMemberId;

    @Column(name = "invited_user_id", nullable = false)
    @Convert(converter = UserIdConverter.class)
    private UserId invitedUserId;

    @Column(name  = "invited_at", nullable = false)
    private Instant invitedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "invitation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private InvitationStatus invitationStatus = InvitationStatus.PENDING;

    private ProjectInvitation(MemberId inviterMemberId, UserId invitedUserId, ProjectId projectId) {
        this.inviterMemberId = inviterMemberId;
        this.invitedUserId = invitedUserId;
        this.projectId = projectId;
    }

    public boolean isExpired(Clock clock) {
        return invitationStatus == InvitationStatus.EXPIRED
                || (invitationStatus == InvitationStatus.PENDING && expiresAt.isBefore(Instant.now(clock)));
    }

    public static ProjectInvitation invite(MemberId inviterMemberId, UserId invitedUserId, ProjectId projectId, Clock clock) {
        ProjectInvitation invitation = new ProjectInvitation(inviterMemberId, invitedUserId, projectId);
        invitation.invitedAt = Instant.now(clock);
        invitation.expiresAt = invitation.invitedAt.plus(Duration.ofDays(7));
        return invitation;
    }
}
