package pl.barkly.school.api;

import pl.barkly.school.SchoolEntity;
import java.util.List;

public record SchoolResponse(
        Long id,
        String name,
        String slug,
        String address,
        String krs,
        String description,
        String activities,
        String pricing,
        List<SchoolImageResponse> images
) {
    public static SchoolResponse from(SchoolEntity school) {
        return new SchoolResponse(school.getId(), school.getName(), school.getSlug(), school.getAddress(),
                school.getKrs(), school.getDescription(), school.getActivities(), school.getPricing(), List.of());
    }
}
