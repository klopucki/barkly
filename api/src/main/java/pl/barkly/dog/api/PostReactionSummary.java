package pl.barkly.dog.api;

import pl.barkly.dog.PostReactionType;

public record PostReactionSummary(PostReactionType reactionType, long count) { }
