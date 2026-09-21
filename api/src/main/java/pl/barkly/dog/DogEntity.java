package pl.barkly.dog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pl.barkly.user.UserEntity;

import java.time.LocalDate;

@Entity
@Table(name = "dog")
public class DogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id")
    private UserEntity owner;

    @Column(nullable = false)
    private String name;

    private String breed;
    private LocalDate birthDate;
    private String sex;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DogVisibility visibility;

    protected DogEntity() {
    }

    DogEntity(UserEntity owner, String name, String breed, LocalDate birthDate, String sex,
              String description, DogVisibility visibility) {
        this.owner = owner;
        this.name = name;
        this.breed = breed;
        this.birthDate = birthDate;
        this.sex = sex;
        this.description = description;
        this.visibility = visibility;
    }

    void update(String name, String breed, LocalDate birthDate, String sex, String description,
                DogVisibility visibility) {
        this.name = name;
        this.breed = breed;
        this.birthDate = birthDate;
        this.sex = sex;
        this.description = description;
        this.visibility = visibility;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    String getBreed() {
        return breed;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    String getSex() {
        return sex;
    }

    String getDescription() {
        return description;
    }

    DogVisibility getVisibility() {
        return visibility;
    }
}
