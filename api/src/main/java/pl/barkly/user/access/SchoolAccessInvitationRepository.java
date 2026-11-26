package pl.barkly.user.access;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

interface SchoolAccessInvitationRepository extends JpaRepository<SchoolAccessInvitation, Long> {
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SchoolAccessInvitation> findById(Long id);

    boolean existsBySchoolIdAndInviteeUserIdAndStatus(
            Long schoolId, Long userId, AccessStatus status);

    Optional<SchoolAccessInvitation> findBySchoolIdAndInviteeUserId(Long schoolId, Long userId);

    long countByInviteeUserIdAndStatus(Long userId, AccessStatus status);

    List<SchoolAccessInvitation> findAllBySchoolId(Long schoolId);

    List<SchoolAccessInvitation> findAllByInviteeUserIdAndStatus(Long userId, AccessStatus status);
}
