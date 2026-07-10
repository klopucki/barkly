package pl.barkly.training.image;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "barkly.images")
public record ImageProperties(
        Path storageDirectory,
        DataSize maxFileSize,
        DataSize maxRequestSize
) {
}
