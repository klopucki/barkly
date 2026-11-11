package pl.barkly.training.image;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import pl.barkly.training.exceptions.InvalidImageException;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

@Component
class S3TrainingImageStorage implements TrainingImageStorage {

    private final S3Client client;
    private final String bucket;
    private final long maxFileSize;
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;

    S3TrainingImageStorage(ImageProperties properties, S3Client client, S3ImageProperties s3) {
        this.client = client;
        this.bucket = s3.bucket();
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
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            writeJpeg(optimized, output);
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromBytes(output.toByteArray()));
            return key;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not encode training image", exception);
        }
    }

    @Override
    public Resource load(String key) {
        validateKey(key);
        try {
            return new ByteArrayResource(
                    client.getObjectAsBytes(
                                    GetObjectRequest.builder().bucket(bucket).key(key).build())
                            .asByteArray());
        } catch (NoSuchKeyException exception) {
            throw new ImageNotFoundException();
        }
    }

    @Override
    public void delete(String key) {
        if (key == null) return;
        validateKey(key);
        client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    private void validateKey(String key) {
        if (key == null || !key.matches("[A-Za-z0-9_-]+\\.(jpg|jpeg|png|webp)")) {
            throw new InvalidImageException("Invalid image key");
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
            boolean jpeg =
                    length >= 3
                            && (header[0] & 0xff) == 0xff
                            && (header[1] & 0xff) == 0xd8
                            && (header[2] & 0xff) == 0xff;
            boolean png =
                    length >= 8
                            && header[0] == (byte) 0x89
                            && header[1] == 0x50
                            && header[2] == 0x4e
                            && header[3] == 0x47;
            if (jpeg || png) return;
        } catch (IOException exception) {
            throw new InvalidImageException("Could not read image file");
        }
        throw new InvalidImageException("Only JPEG and PNG images are supported");
    }

    private void validateDimensions(BufferedImage image) {
        if (image.getWidth() < minWidth || image.getHeight() < minHeight) {
            throw new InvalidImageException(
                    "Image must be at least " + minWidth + " × " + minHeight + " pixels");
        }
    }

    private BufferedImage resize(BufferedImage source) {
        double ratio =
                Math.min(
                        1d,
                        Math.min(
                                (double) maxWidth / source.getWidth(),
                                (double) maxHeight / source.getHeight()));
        int width = (int) Math.round(source.getWidth() * ratio);
        int height = (int) Math.round(source.getHeight() * ratio);
        BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setColor(java.awt.Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private void writeJpeg(BufferedImage image, OutputStream output) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        try (ImageOutputStream stream = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(stream);
            ImageWriteParam parameters = writer.getDefaultWriteParam();
            parameters.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            parameters.setCompressionQuality(0.85f);
            writer.write(null, new IIOImage(image, null, null), parameters);
        } finally {
            writer.dispose();
        }
    }
}
