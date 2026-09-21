package pl.barkly.dog.api;

import jakarta.validation.constraints.NotNull;
import pl.barkly.dog.PostReactionType;

public record PostReactionRequest(@NotNull PostReactionType reactionType) { }
