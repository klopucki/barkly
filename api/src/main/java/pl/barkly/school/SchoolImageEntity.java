package pl.barkly.school;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "school_image")
public class SchoolImageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id")
    private SchoolEntity school;

    private String imageKey;

    protected SchoolImageEntity() { }
    public SchoolImageEntity(SchoolEntity school, String imageKey) { this.school = school; this.imageKey = imageKey; }
    public Long getId() { return id; }
    public Long getSchoolId() { return school.getId(); }
    public String getImageKey() { return imageKey; }
}
