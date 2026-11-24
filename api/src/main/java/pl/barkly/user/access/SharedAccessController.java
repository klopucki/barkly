package pl.barkly.user.access;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access")
class SharedAccessController {
    private final SharedAccessService access;

    SharedAccessController(SharedAccessService access) {
        this.access = access;
    }

    record InviteRequest(@NotBlank @Email String email) {}

    @GetMapping("/invitations")
    List<SharedAccessService.Invitation> pending() {
        return access.pending();
    }

    @GetMapping("/schools/{id}")
    List<SharedAccessService.Invitation> school(@PathVariable Long id) {
        return access.schoolInvitations(id);
    }

    @GetMapping("/dogs/{id}")
    List<SharedAccessService.Invitation> dog(@PathVariable Long id) {
        return access.dogInvitations(id);
    }

    @PostMapping("/schools/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void inviteSchool(@PathVariable Long id, @Valid @RequestBody InviteRequest request) {
        access.inviteSchool(id, request.email());
    }

    @PostMapping("/dogs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void inviteDog(@PathVariable Long id, @Valid @RequestBody InviteRequest request) {
        access.inviteDog(id, request.email());
    }

    @PostMapping("/school-invitations/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void acceptSchool(@PathVariable Long id) {
        access.acceptSchool(id);
    }

    @PostMapping("/dog-invitations/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void acceptDog(@PathVariable Long id) {
        access.acceptDog(id);
    }

    @DeleteMapping("/school-invitations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revokeSchool(@PathVariable Long id) {
        access.revokeSchool(id);
    }

    @DeleteMapping("/dog-invitations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void revokeDog(@PathVariable Long id) {
        access.revokeDog(id);
    }
}
