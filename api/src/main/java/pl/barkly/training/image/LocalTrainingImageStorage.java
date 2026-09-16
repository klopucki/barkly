package pl.barkly.training.image;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.barkly.training.exceptions.InvalidImageException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
class LocalTrainingImageStorage implements TrainingImageStorage {

    private final Path storageDirectory;
    private final long maxFileSize;
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;

    LocalTrainingImageStorage(ImageProperties properties) {
        this.storageDirectory = properties.storageDirectory().toAbsolutePath().normalize();
        this.maxFileSize = properties.maxFileSize().toBytes();
        this.minWidth = properties.minWidth();
        this.minHeight = properties.minHeight();
        this.maxWidth = properties.maxWidth();
        this.maxHeight = properties.maxHeight();
    }

    @Override
    public String store(MultipartFile image) {
        validateSize(image);
        validateFormat(image);
        BufferedImage source = readImage(image);
        validateDimensions(source);
        BufferedImage optimized = resize(source);
        String key = UUID.randomUUID() + ".jpg";

        try {
            Files.createDirectories(storageDirectory);
            writeJpeg(optimized, resolve(key));
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

    private BufferedImage readImage(MultipartFile image) {
        try (InputStream input = image.getInputStream()) {
            BufferedImage decoded = ImageIO.read(input);
            if (decoded != null) return decoded;
        } catch (IOException exception) {
            throw new InvalidImageException("Could not read image file");
        }
        throw new InvalidImageException("Only JPEG and PNG images are supported");
    }

    private void validateFormat(MultipartFile image) {
        byte[] header = new byte[8];
        try (InputStream input = image.getInputStream()) {
            int length = input.read(header);
            boolean jpeg = length >= 3 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff;
            boolean png = length >= 8 && header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4e && header[3] == 0x47;
            if (jpeg || png) return;
        } catch (IOException exception) {
            throw new InvalidImageException("Could not read image file");
        }
        throw new InvalidImageException("Only JPEG and PNG images are supported");
    }

    private void validateDimensions(BufferedImage image) {
        if (image.getWidth() < minWidth || image.getHeight() < minHeight) {
            throw new InvalidImageException("Image must be at least " + minWidth + " × " + minHeight + " pixels");
        }
    }

    private BufferedImage resize(BufferedImage source) {
        double ratio = Math.min(1d, Math.min((double) maxWidth / source.getWidth(), (double) maxHeight / source.getHeight()));
        int width = (int) Math.round(source.getWidth() * ratio);
        int height = (int) Math.round(source.getHeight() * ratio);
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setColor(java.awt.Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private void writeJpeg(BufferedImage image, Path destination) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        try (OutputStream output = Files.newOutputStream(destination); ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(stream);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parameters.setCompressionQuality(0.85f);
            writer.write(null, new IIOImage(image, null, null), parameters);
        } finally {
            writer.dispose();
        }
    }

    private Path resolve(String key) {
        Path path = storageDirectory.resolve(key).normalize();
        if (!path.getParent().equals(storageDirectory)) {
            throw new InvalidImageException("Invalid image key");
        }
        return path;
    }
}
