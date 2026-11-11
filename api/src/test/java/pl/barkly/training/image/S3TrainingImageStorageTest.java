package pl.barkly.training.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import pl.barkly.training.exceptions.InvalidImageException;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.HashMap;

class S3TrainingImageStorageTest {

    @Test
    void storesLoadsAndDeletesSupportedImage() throws Exception {
        var storage = storageWithLimit(DataSize.ofKilobytes(10));
        byte[] png = png(800, 600);
        var file = new MockMultipartFile("image", "dog.png", "image/png", png);

        String key = storage.store(file);

        assertThat(key).endsWith(".jpg");
        assertThat(storage.load(key).getContentAsByteArray()).isNotEmpty();

        storage.delete(key);
        assertThatThrownBy(() -> storage.load(key)).isInstanceOf(ImageNotFoundException.class);
    }

    @Test
    void rejectsUnsupportedContentEvenWhenMimeTypeClaimsImage() {
        var storage = storageWithLimit(DataSize.ofKilobytes(10));
        var file =
                new MockMultipartFile("image", "fake.png", "image/png", "not an image".getBytes());

        assertThatThrownBy(() -> storage.store(file))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("JPEG and PNG");
    }

    @Test
    void rejectsFileOverConfiguredLimit() throws Exception {
        var storage = storageWithLimit(DataSize.ofBytes(3));
        var file = new MockMultipartFile("image", "dog.jpg", "image/jpeg", png(800, 600));

        assertThatThrownBy(() -> storage.store(file))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("size limit");
    }

    @Test
    void scalesLargeImagesToConfiguredDimensions() throws Exception {
        var storage = storageWithLimit(DataSize.ofMegabytes(10));
        var file = new MockMultipartFile("image", "large.png", "image/png", png(2400, 1800));

        String key = storage.store(file);

        var stored = javax.imageio.ImageIO.read(storage.load(key).getInputStream());
        assertThat(stored.getWidth()).isEqualTo(1600);
        assertThat(stored.getHeight()).isEqualTo(1200);
    }

    private S3TrainingImageStorage storageWithLimit(DataSize limit) {
        var objects = new HashMap<String, byte[]>();
        S3Client client = mock(S3Client.class);
        when(client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenAnswer(
                        call -> {
                            PutObjectRequest request = call.getArgument(0);
                            RequestBody body = call.getArgument(1);
                            objects.put(
                                    request.key(),
                                    body.contentStreamProvider().newStream().readAllBytes());
                            return PutObjectResponse.builder().build();
                        });
        when(client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenAnswer(
                        call -> {
                            GetObjectRequest request = call.getArgument(0);
                            byte[] bytes = objects.get(request.key());
                            if (bytes == null) throw NoSuchKeyException.builder().build();
                            return ResponseBytes.fromByteArray(
                                    GetObjectResponse.builder().build(), bytes);
                        });
        when(client.deleteObject(any(DeleteObjectRequest.class)))
                .thenAnswer(
                        call -> {
                            DeleteObjectRequest request = call.getArgument(0);
                            objects.remove(request.key());
                            return DeleteObjectResponse.builder().build();
                        });
        return new S3TrainingImageStorage(
                new ImageProperties(limit, limit, 640, 480, 1600, 1600),
                client,
                new S3ImageProperties("", "us-east-1", "test", true));
    }

    private byte[] png(int width, int height) throws Exception {
        var image =
                new java.awt.image.BufferedImage(
                        width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var output = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
