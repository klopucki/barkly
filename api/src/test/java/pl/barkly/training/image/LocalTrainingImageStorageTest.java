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
        byte[] png = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        var file = new MockMultipartFile("image", "dog.png", "image/png", png);

        String key = storage.store(file);

        assertThat(key).endsWith(".png");
        assertThat(storage.load(key).getContentAsByteArray()).isEqualTo(png);

        storage.delete(key);
        assertThatThrownBy(() -> storage.load(key)).isInstanceOf(InvalidImageException.class);
    }

    @Test
    void rejectsUnsupportedContentEvenWhenMimeTypeClaimsImage() {
        var storage = storageWithLimit(DataSize.ofKilobytes(10));
        var file = new MockMultipartFile("image", "fake.png", "image/png", "not an image".getBytes());

        assertThatThrownBy(() -> storage.store(file))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("JPEG, PNG and WebP");
    }

    @Test
    void rejectsFileOverConfiguredLimit() {
        var storage = storageWithLimit(DataSize.ofBytes(3));
        var file = new MockMultipartFile("image", "dog.jpg", "image/jpeg",
                new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00});

        assertThatThrownBy(() -> storage.store(file))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("size limit");
    }

    private LocalTrainingImageStorage storageWithLimit(DataSize limit) {
        return new LocalTrainingImageStorage(new ImageProperties(directory, limit, limit));
    }
}
