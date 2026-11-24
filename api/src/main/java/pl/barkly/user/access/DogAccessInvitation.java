package pl.barkly.user.access;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "dog_access_invitation")
class DogAccessInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dog_id", nullable = false)
    private Long dogId;

    @Column(name = "invitee_user_id", nullable = false)
    private Long inviteeUserId;

    @Column(name = "invited_by_user_id", nullable = false)
    private Long invitedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DogAccessInvitation() {}

    DogAccessInvitation(Long dogId, Long inviteeUserId, Long invitedByUserId) {
        this.dogId = dogId;
        this.inviteeUserId = inviteeUserId;
        this.invitedByUserId = invitedByUserId;
        this.status = AccessStatus.PENDING;
        this.createdAt = Instant.now();
    }

    Long getId() {
        return id;
    }

    Long getDogId() {
        return dogId;
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
