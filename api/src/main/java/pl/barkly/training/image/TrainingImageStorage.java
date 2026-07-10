package pl.barkly.training.image;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface TrainingImageStorage {

    String store(MultipartFile image);

    Resource load(String key);

    void delete(String key);
}
