package pl.barkly.training.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;

import javax.imageio.ImageIO;

@EnabledIfEnvironmentVariable(named = "BARKLY_TEST_S3_ENDPOINT", matches = ".+")
class S3TrainingImageStorageIntegrationTest {
    @Test
    void uploadsReadsAndDeletesImage() throws Exception {
        try (var client =
                S3Client.builder()
                        .endpointOverride(URI.create(System.getenv("BARKLY_TEST_S3_ENDPOINT")))
                        .region(Region.US_EAST_1)
                        .credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(
                                                "barkly-local", "barkly-local-password")))
                        .serviceConfiguration(
                                S3Configuration.builder().pathStyleAccessEnabled(true).build())
                        .build()) {
            var properties =
                    new ImageProperties(
                            DataSize.ofMegabytes(10),
                            DataSize.ofMegabytes(12),
                            640,
                            480,
                            1600,
                            1600);
            var storage =
                    new S3TrainingImageStorage(
                            properties,
                            client,
                            new S3ImageProperties("", "us-east-1", "barkly-images", true));
            var output = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB), "png", output);
            String key =
                    storage.store(
                            new MockMultipartFile(
                                    "image", "test.png", "image/png", output.toByteArray()));
            try {
                assertThat(ImageIO.read(storage.load(key).getInputStream()).getWidth())
                        .isEqualTo(800);
            } finally {
                storage.delete(key);
            }
            assertThatThrownBy(() -> storage.load(key)).isInstanceOf(ImageNotFoundException.class);
        }
    }
}
