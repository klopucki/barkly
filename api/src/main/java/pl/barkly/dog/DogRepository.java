package pl.barkly.dog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DogRepository extends JpaRepository<DogEntity, Long> {
    List<DogEntity> findAllByOwner_IdOrderByNameAsc(Long ownerId);

    @Query(
            """
            select dog from DogEntity dog
            where dog.owner.id = :userId or exists (
                select invitation.id from DogAccessInvitation invitation
                where invitation.dogId = dog.id and invitation.inviteeUserId = :userId
                  and invitation.status = pl.barkly.user.access.AccessStatus.ACCEPTED)
            order by dog.name
            """)
    List<DogEntity> findAccessibleByUserId(Long userId);

    List<DogEntity> findAllByOrderByNameAsc();
}
