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

@Entity
@Table(name = "dog_image")
class DogImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dog_id")
    private DogEntity dog;

    @Column(nullable = false)
    private String imageKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DogVisibility visibility;

    protected DogImageEntity() {
    }

    DogImageEntity(DogEntity dog, String imageKey, DogVisibility visibility) {
        this.dog = dog;
        this.imageKey = imageKey;
        this.visibility = visibility;
    }

    Long getId() {
        return id;
    }

    DogEntity getDog() {
        return dog;
    }

    String getImageKey() {
        return imageKey;
    }

    DogVisibility getVisibility() {
        return visibility;
    }
}
