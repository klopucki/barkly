package pl.barkly.training.image;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "barkly.images")
public record ImageProperties(
        DataSize maxFileSize,
        DataSize maxRequestSize,
        int minWidth,
        int minHeight,
        int maxWidth,
        int maxHeight) {}
