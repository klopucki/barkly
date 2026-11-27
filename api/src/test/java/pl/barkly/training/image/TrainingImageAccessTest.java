package pl.barkly.training.image;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import pl.barkly.school.SchoolImageRepository;
import pl.barkly.school.SchoolService;
import pl.barkly.training.exceptions.ResourceNotFoundException;
import pl.barkly.training.persistence.BookingRepository;
import pl.barkly.training.persistence.TrainingRepository;

class TrainingImageAccessTest {
    @Test
    void genericImageUrlOnlyLoadsReferencedTrainingOrSchoolImages() {
        var trainings = mock(TrainingRepository.class);
        var schools = mock(SchoolImageRepository.class);
        var storage = mock(TrainingImageStorage.class);
        var service =
                new TrainingImageService(
                        trainings,
                        mock(BookingRepository.class),
                        storage,
                        mock(SchoolService.class),
                        schools);
        when(storage.load(anyString())).thenReturn(new ByteArrayResource(new byte[] {1}));
        when(trainings.existsByImageKeyAndDeletedAtIsNull("training.jpg")).thenReturn(true);
        when(schools.existsByImageKey("school.jpg")).thenReturn(true);
        assertThat(service.load("training.jpg")).isNotNull();
        assertThat(service.load("school.jpg")).isNotNull();
        assertThatThrownBy(() -> service.load("dog-post.jpg"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(storage, never()).load("dog-post.jpg");
    }
}
