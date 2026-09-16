package pl.barkly.training.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import pl.barkly.training.exceptions.InvalidImageException;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalTrainingImageStorageTest {

    @TempDir
    Path directory;

    @Test
    void storesLoadsAndDeletesSupportedImage() throws Exception {
        var storage = storageWithLimit(DataSize.ofKilobytes(10));
        byte[] png = png(800, 600);
        var file = new MockMultipartFile("image", "dog.png", "image/png", png);

        String key = storage.store(file);

        assertThat(key).endsWith(".jpg");
        assertThat(storage.load(key).getContentAsByteArray()).isNotEmpty();

        storage.delete(key);
        assertThatThrownBy(() -> storage.load(key)).isInstanceOf(InvalidImageException.class);
    }

    @Test
    void rejectsUnsupportedContentEvenWhenMimeTypeClaimsImage() {
        var storage = storageWithLimit(DataSize.ofKilobytes(10));
        var file = new MockMultipartFile("image", "fake.png", "image/png", "not an image".getBytes());

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

    private LocalTrainingImageStorage storageWithLimit(DataSize limit) {
        return new LocalTrainingImageStorage(new ImageProperties(directory, limit, limit, 640, 480, 1600, 1600));
    }

    private byte[] png(int width, int height) throws Exception {
        var image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        var output = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "png", output);
        return output.toByteArray();
    }
}
