package pl.barkly.dog.api;

import pl.barkly.dog.PostReactionType;

import java.time.LocalDateTime;
import java.util.List;

public record DogPostResponse(
        Long id,
        Long dogId,
        String dogName,
        String ownerDisplayName,
        String content,
        boolean hasImage,
        LocalDateTime publishedAt,
        List<PostReactionSummary> reactions,
        PostReactionType myReaction,
        long commentCount) {}
