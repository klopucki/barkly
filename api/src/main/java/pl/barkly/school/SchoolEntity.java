package pl.barkly.school;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pl.barkly.school.api.SchoolCreateRequest;
import pl.barkly.user.UserEntity;

@Entity
@Table(name = "school")
public class SchoolEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String address;

    private String krs;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String activities;

    @Column(nullable = false)
    private String pricing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id")
    private UserEntity owner;

    protected SchoolEntity() {
    }

    public SchoolEntity(SchoolCreateRequest request, UserEntity owner) {
        this.name = request.name().trim();
        this.slug = "pending";
        this.address = request.address().trim();
        this.krs = emptyToNull(request.krs());
        this.description = empty(request.description());
        this.activities = empty(request.activities());
        this.pricing = empty(request.pricing());
        this.owner = owner;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getAddress() { return address; }
    public String getKrs() { return krs; }
    public String getDescription() { return description; }
    public String getActivities() { return activities; }
    public String getPricing() { return pricing; }
    public UserEntity getOwner() { return owner; }

    public void setGeneratedSlug(String slug) { this.slug = slug; }

    public void update(SchoolCreateRequest request) {
        this.name = request.name().trim();
        this.address = request.address().trim();
        this.krs = emptyToNull(request.krs());
        this.description = empty(request.description());
        this.activities = empty(request.activities());
        this.pricing = empty(request.pricing());
    }

    private static String empty(String value) { return value == null ? "" : value.trim(); }
    private static String emptyToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
