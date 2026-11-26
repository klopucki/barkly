package pl.barkly.user.access;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

interface DogAccessInvitationRepository extends JpaRepository<DogAccessInvitation, Long> {
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DogAccessInvitation> findById(Long id);

    boolean existsByDogIdAndInviteeUserIdAndStatus(Long dogId, Long userId, AccessStatus status);

    Optional<DogAccessInvitation> findByDogIdAndInviteeUserId(Long dogId, Long userId);

    long countByInviteeUserIdAndStatus(Long userId, AccessStatus status);

    List<DogAccessInvitation> findAllByDogId(Long dogId);

    List<DogAccessInvitation> findAllByInviteeUserIdAndStatus(Long userId, AccessStatus status);
}
