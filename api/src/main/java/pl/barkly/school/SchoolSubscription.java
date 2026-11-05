package pl.barkly.school;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "school_subscription")
@IdClass(SchoolSubscription.Key.class)
public class SchoolSubscription {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "school_id")
    private Long schoolId;

    protected SchoolSubscription() {}

    public static class Key implements Serializable {
        public Long userId;
        public Long schoolId;

        public Key() {}

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key
                    && Objects.equals(userId, key.userId)
                    && Objects.equals(schoolId, key.schoolId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, schoolId);
        }
    }
}
