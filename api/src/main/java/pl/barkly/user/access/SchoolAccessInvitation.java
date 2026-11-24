package pl.barkly.user.access;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "school_access_invitation")
class SchoolAccessInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "invitee_user_id", nullable = false)
    private Long inviteeUserId;

    @Column(name = "invited_by_user_id", nullable = false)
    private Long invitedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SchoolAccessInvitation() {}

    SchoolAccessInvitation(Long schoolId, Long inviteeUserId, Long invitedByUserId) {
        this.schoolId = schoolId;
        this.inviteeUserId = inviteeUserId;
        this.invitedByUserId = invitedByUserId;
        this.status = AccessStatus.PENDING;
        this.createdAt = Instant.now();
    }

    Long getId() {
        return id;
    }

    Long getSchoolId() {
        return schoolId;
    }

    Long getInviteeUserId() {
        return inviteeUserId;
    }

    Long getInvitedByUserId() {
        return invitedByUserId;
    }

    AccessStatus getStatus() {
        return status;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    void accept() {
        status = AccessStatus.ACCEPTED;
    }
}
