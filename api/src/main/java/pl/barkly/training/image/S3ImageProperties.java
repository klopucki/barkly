package pl.barkly.training.image;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "barkly.images.s3")
record S3ImageProperties(String endpoint, String region, String bucket, boolean pathStyle) {}
