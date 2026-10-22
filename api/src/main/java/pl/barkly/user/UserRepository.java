package pl.barkly.user;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    @Query(
            """
            select u from UserEntity u
            where lower(u.email) like lower(concat('%', :query, '%')) escape '!'
               or lower(u.displayName) like lower(concat('%', :query, '%')) escape '!'
            """)
    Page<UserEntity> search(String query, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            select u from UserEntity u
            where u.role = pl.barkly.user.UserRole.SUPER_ADMIN order by u.id
            """)
    List<UserEntity> lockAdministrators();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.id = :id")
    Optional<UserEntity> findLockedById(Long id);
}
