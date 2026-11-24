package pl.barkly.user.access;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import pl.barkly.dog.DogEntity;
import pl.barkly.dog.DogRepository;
import pl.barkly.school.SchoolEntity;
import pl.barkly.school.SchoolRepository;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserRepository;
import pl.barkly.user.UserRole;
import pl.barkly.user.UserService;

import java.time.Instant;
import java.util.List;

@Service
public class SharedAccessService {
    private final SchoolAccessInvitationRepository schools;
    private final DogAccessInvitationRepository dogs;
    private final SchoolRepository schoolRepository;
    private final DogRepository dogRepository;
    private final UserRepository userRepository;
    private final UserService users;

    SharedAccessService(
            SchoolAccessInvitationRepository schools,
            DogAccessInvitationRepository dogs,
            SchoolRepository schoolRepository,
            DogRepository dogRepository,
            UserRepository userRepository,
            UserService users) {
        this.schools = schools;
        this.dogs = dogs;
        this.schoolRepository = schoolRepository;
        this.dogRepository = dogRepository;
        this.userRepository = userRepository;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public boolean canManageSchool(SchoolEntity school, UserEntity user) {
        return user.getRole() == UserRole.SUPER_ADMIN
                || (user.getRole() == UserRole.SCHOOL_ADMIN
                        && school.getOwner().getId().equals(user.getId()))
                || schools.existsBySchoolIdAndInviteeUserIdAndStatus(
                        school.getId(), user.getId(), AccessStatus.ACCEPTED);
    }

    @Transactional(readOnly = true)
    public boolean canManageDog(DogEntity dog, UserEntity user) {
        return dog.getOwner().getId().equals(user.getId())
                || dogs.existsByDogIdAndInviteeUserIdAndStatus(
                        dog.getId(), user.getId(), AccessStatus.ACCEPTED);
    }

    private UserEntity invitee(String email, UserEntity current) {
        UserEntity user =
                userRepository
                        .findByEmailIgnoreCase(email.trim())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "User not found"));
        if (!user.isActive() || user.getId().equals(current.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitee");
        return user;
    }

    private SchoolEntity school(Long id) {
        return schoolRepository
                .findWithOwnerById(id)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "School not found"));
    }

    private DogEntity dog(Long id) {
        return dogRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));
    }

    @Transactional
    public void inviteSchool(Long id, String email) {
        UserEntity current = users.currentUser();
        SchoolEntity school = school(id);
        if (!canManageSchool(school, current))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        UserEntity target = invitee(email, current);
        if (target.getId().equals(school.getOwner().getId())
                || schools.findBySchoolIdAndInviteeUserId(id, target.getId()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Access already exists");
        schools.save(new SchoolAccessInvitation(id, target.getId(), current.getId()));
    }

    @Transactional
    public void inviteDog(Long id, String email) {
        UserEntity current = users.currentUser();
        DogEntity dog = dog(id);
        if (!dog.getOwner().getId().equals(current.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        UserEntity target = invitee(email, current);
        if (dogs.findByDogIdAndInviteeUserId(id, target.getId()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Access already exists");
        dogs.save(new DogAccessInvitation(id, target.getId(), current.getId()));
    }

    @Transactional
    public void acceptSchool(Long id) {
        SchoolAccessInvitation invitation =
                schools.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!invitation.getInviteeUserId().equals(users.currentUser().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        invitation.accept();
    }

    @Transactional
    public void acceptDog(Long id) {
        DogAccessInvitation invitation =
                dogs.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!invitation.getInviteeUserId().equals(users.currentUser().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        invitation.accept();
    }

    @Transactional(readOnly = true)
    public List<Invitation> pending() {
        Long userId = users.currentUser().getId();
        var result = new java.util.ArrayList<Invitation>();
        for (var invitation :
                schools.findAllByInviteeUserIdAndStatus(userId, AccessStatus.PENDING)) {
            var school = school(invitation.getSchoolId());
            result.add(
                    new Invitation(
                            invitation.getId(),
                            "SCHOOL",
                            school.getId(),
                            school.getName(),
                            userId,
                            users.currentUser().getDisplayName(),
                            invitation.getStatus().name(),
                            invitation.getCreatedAt()));
        }
        for (var invitation : dogs.findAllByInviteeUserIdAndStatus(userId, AccessStatus.PENDING)) {
            var dog = dog(invitation.getDogId());
            result.add(
                    new Invitation(
                            invitation.getId(),
                            "DOG",
                            dog.getId(),
                            dog.getName(),
                            userId,
                            users.currentUser().getDisplayName(),
                            invitation.getStatus().name(),
                            invitation.getCreatedAt()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Invitation> schoolInvitations(Long id) {
        SchoolEntity school = school(id);
        if (!canManageSchool(school, users.currentUser()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return schools.findAllBySchoolId(id).stream()
                .map(
                        i -> {
                            UserEntity user =
                                    userRepository.findById(i.getInviteeUserId()).orElseThrow();
                            return new Invitation(
                                    i.getId(),
                                    "SCHOOL",
                                    id,
                                    school.getName(),
                                    user.getId(),
                                    user.getDisplayName(),
                                    i.getStatus().name(),
                                    i.getCreatedAt());
                        })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Invitation> dogInvitations(Long id) {
        DogEntity dog = dog(id);
        if (!dog.getOwner().getId().equals(users.currentUser().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return dogs.findAllByDogId(id).stream()
                .map(
                        i -> {
                            UserEntity user =
                                    userRepository.findById(i.getInviteeUserId()).orElseThrow();
                            return new Invitation(
                                    i.getId(),
                                    "DOG",
                                    id,
                                    dog.getName(),
                                    user.getId(),
                                    user.getDisplayName(),
                                    i.getStatus().name(),
                                    i.getCreatedAt());
                        })
                .toList();
    }

    @Transactional
    public void revokeSchool(Long id) {
        var invitation =
                schools.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var current = users.currentUser();
        if (!invitation.getInviteeUserId().equals(current.getId())
                && !canManageSchool(school(invitation.getSchoolId()), current))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        schools.delete(invitation);
    }

    @Transactional
    public void revokeDog(Long id) {
        var invitation =
                dogs.findById(id)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var current = users.currentUser();
        if (!invitation.getInviteeUserId().equals(current.getId())
                && !dog(invitation.getDogId()).getOwner().getId().equals(current.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        dogs.delete(invitation);
    }

    public record Invitation(
            Long id,
            String type,
            Long resourceId,
            String resourceName,
            Long inviteeId,
            String inviteeName,
            String status,
            Instant createdAt) {}
}
