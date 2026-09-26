package io.github.kingarturito.projile.project.internal.entities;

import io.github.kingarturito.projile.project.ProjectId;
import io.github.kingarturito.projile.project.internal.enums.InvitationStatus;
import io.github.kingarturito.projile.project.internal.valueobjects.InvitationId;
import io.github.kingarturito.projile.project.internal.valueobjects.MemberId;
import io.github.kingarturito.projile.user.UserId;
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

    @EmbeddedId
    @AttributeOverride(
            name = "invitationId",
            column = @Column(name = "invitation_id", nullable = false, columnDefinition = "uuid"))
    @EqualsAndHashCode.Include
    private InvitationId invitationId = InvitationId.newId();

    @Embedded
    @AttributeOverride(
            name = "projectId",
            column = @Column(name = "project_id", nullable = false, columnDefinition = "uuid")
    )
    private ProjectId projectId;

    @Embedded
    @AttributeOverride(
            name = "memberId",
            column = @Column(name = "inviter_member_id", nullable = false, columnDefinition = "uuid")
    )
    private MemberId inviterMemberId;

    @Embedded
    @AttributeOverride(
            name = "userId",
            column = @Column(name = "invited_user_id", nullable = false, columnDefinition = "uuid")
    )
    private UserId invitedUserId;

    @Column(name  = "invited_at", nullable = false)
    private Instant invitedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "invitation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private InvitationStatus invitationStatus = InvitationStatus.PENDING;

    @Column(name = "invitation_message", nullable = true, length = 1000)
    private String invitationMessage;

    private ProjectInvitation(MemberId inviterMemberId, UserId invitedUserId, ProjectId projectId, String invitationMessage) {
        this.inviterMemberId = inviterMemberId;
        this.invitedUserId = invitedUserId;
        this.projectId = projectId;
        this.invitationMessage = invitationMessage;
    }

    public boolean isExpired(Clock clock) {
        return invitationStatus == InvitationStatus.EXPIRED
                || (invitationStatus == InvitationStatus.PENDING && expiresAt.isBefore(Instant.now(clock)));
    }

    public static ProjectInvitation invite(MemberId inviterMemberId, UserId invitedUserId, ProjectId projectId, String invitationMessage, Clock clock) {
        ProjectInvitation invitation = new ProjectInvitation(inviterMemberId, invitedUserId, projectId, invitationMessage);
        invitation.invitedAt = Instant.now(clock);
        invitation.expiresAt = invitation.invitedAt.plus(Duration.ofDays(7));
        return invitation;
    }
}