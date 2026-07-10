package pl.barkly.training.image;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.barkly.training.exceptions.InvalidImageException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
class LocalTrainingImageStorage implements TrainingImageStorage {

    private final Path storageDirectory;
    private final long maxFileSize;

    LocalTrainingImageStorage(ImageProperties properties) {
        this.storageDirectory = properties.storageDirectory().toAbsolutePath().normalize();
        this.maxFileSize = properties.maxFileSize().toBytes();
    }

    @Override
    public String store(MultipartFile image) {
        validateSize(image);
        ImageType type = detectType(image);
        String key = UUID.randomUUID() + type.extension;

        try {
            Files.createDirectories(storageDirectory);
            try (InputStream input = image.getInputStream()) {
                Files.copy(input, resolve(key), StandardCopyOption.REPLACE_EXISTING);
            }
            return key;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store training image", exception);
        }
    }

    @Override
    public Resource load(String key) {
        try {
            Resource resource = new UrlResource(resolve(key).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new InvalidImageException("Image not found");
            }
            return resource;
        } catch (MalformedURLException exception) {
            throw new InvalidImageException("Invalid image key");
        }
    }

    @Override
    public void delete(String key) {
        if (key == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not delete training image", exception);
        }
    }

    private void validateSize(MultipartFile image) {
        if (image.isEmpty()) {
            throw new InvalidImageException("Image file is required");
        }
        if (image.getSize() > maxFileSize) {
            throw new InvalidImageException("Image exceeds the configured size limit");
        }
    }

    private ImageType detectType(MultipartFile image) {
        byte[] header = new byte[12];
        try (InputStream input = image.getInputStream()) {
            int length = input.read(header);
            if (length >= 3 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff) {
                return ImageType.JPEG;
            }
            if (length >= 8 && header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47) {
                return ImageType.PNG;
            }
            if (length >= 12 && matches(header, 0, "RIFF") && matches(header, 8, "WEBP")) {
                return ImageType.WEBP;
            }
        } catch (IOException exception) {
            throw new InvalidImageException("Could not read image file");
        }
        throw new InvalidImageException("Only JPEG, PNG and WebP images are supported");
    }

    private boolean matches(byte[] bytes, int offset, String value) {
        for (int index = 0; index < value.length(); index++) {
            if (bytes[offset + index] != value.charAt(index)) {
                return false;
            }
        }
        return true;
    }

    private Path resolve(String key) {
        Path path = storageDirectory.resolve(key).normalize();
        if (!path.getParent().equals(storageDirectory)) {
            throw new InvalidImageException("Invalid image key");
        }
        return path;
    }

    private enum ImageType {
        JPEG(".jpg"), PNG(".png"), WEBP(".webp");

        private final String extension;

        ImageType(String extension) {
            this.extension = extension;
        }
    }
}
