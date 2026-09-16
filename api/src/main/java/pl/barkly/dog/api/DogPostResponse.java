package pl.barkly.dog.api;

import java.time.LocalDateTime;

public record DogPostResponse(
        Long id,
        Long dogId,
        String dogName,
        String ownerDisplayName,
        String content,
        boolean hasImage,
        LocalDateTime publishedAt
) { }
