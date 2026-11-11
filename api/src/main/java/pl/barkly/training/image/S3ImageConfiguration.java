package pl.barkly.training.image;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3ImageProperties.class)
class S3ImageConfiguration {
    @Bean
    S3Client imageS3Client(S3ImageProperties properties) {
        var builder =
                S3Client.builder()
                        .region(Region.of(properties.region()))
                        .serviceConfiguration(
                                S3Configuration.builder()
                                        .pathStyleAccessEnabled(properties.pathStyle())
                                        .build());
        if (StringUtils.hasText(properties.endpoint()))
            builder.endpointOverride(URI.create(properties.endpoint()));
        return builder.build();
    }
}
