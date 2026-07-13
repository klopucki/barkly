package pl.barkly.school.api;

import pl.barkly.school.SchoolImageEntity;

public record SchoolImageResponse(Long id, String imageKey) {
    public static SchoolImageResponse from(SchoolImageEntity image) {
        return new SchoolImageResponse(image.getId(), image.getImageKey());
    }
}
