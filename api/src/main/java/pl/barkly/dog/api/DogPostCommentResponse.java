package pl.barkly.dog.api;

import java.time.LocalDateTime;

public record DogPostCommentResponse(Long id, String authorDisplayName, String content, LocalDateTime publishedAt, boolean mine) { }
