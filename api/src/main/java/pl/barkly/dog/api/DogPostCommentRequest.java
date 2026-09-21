package pl.barkly.dog.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DogPostCommentRequest(@NotBlank @Size(max = 1000) String content) { }
