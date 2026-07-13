package pl.barkly.school.news;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.barkly.school.SchoolService;
import pl.barkly.school.news.api.*;
import pl.barkly.user.UserService;
import java.util.List;

@RestController
class SchoolNewsController {
 private final SchoolNewsRepository news; private final SchoolService schools; private final UserService users;
 SchoolNewsController(SchoolNewsRepository news, SchoolService schools, UserService users){this.news=news;this.schools=schools;this.users=users;}
 @GetMapping("/api/news") List<SchoolNewsResponse> all(){return news.findAllByOrderByPublishedAtDesc().stream().map(SchoolNewsResponse::from).toList();}
 @GetMapping("/api/schools/{schoolId}/news") List<SchoolNewsResponse> bySchool(@PathVariable Long schoolId){return news.findAllBySchoolIdOrderByPublishedAtDesc(schoolId).stream().map(SchoolNewsResponse::from).toList();}
 @GetMapping("/api/my/schools/{schoolId}/news") List<SchoolNewsResponse> managedBySchool(@PathVariable Long schoolId){var school=schools.findEntityWithOwner(schoolId);schools.requireManagementAccess(school);return news.findAllManagedBySchoolIdOrderByPublishedAtDesc(schoolId).stream().map(SchoolNewsResponse::from).toList();}
 @PostMapping("/api/schools/{schoolId}/news") SchoolNewsResponse create(@PathVariable Long schoolId,@Valid @RequestBody SchoolNewsRequest request){var school=schools.findEntityWithOwner(schoolId);schools.requireManagementAccess(school);return SchoolNewsResponse.from(news.save(new SchoolNewsEntity(school,users.currentUser(),request)));}
 @PutMapping("/api/news/{id}") SchoolNewsResponse update(@PathVariable Long id,@Valid @RequestBody SchoolNewsRequest request){var item=get(id);schools.requireManagementAccess(schools.findEntityWithOwner(item.getSchoolId()));item.update(request);news.save(item);return SchoolNewsResponse.from(item);}
 @DeleteMapping("/api/news/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void delete(@PathVariable Long id){var item=get(id);schools.requireManagementAccess(schools.findEntityWithOwner(item.getSchoolId()));news.delete(item);}
 private SchoolNewsEntity get(Long id){return news.findWithDetailsById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"News not found"));}
}
