package pl.barkly.school.news.api;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record SchoolNewsRequest(@NotBlank @Size(min=3,max=200) String title, @NotBlank @Size(max=10000) String content, Boolean active) { }
