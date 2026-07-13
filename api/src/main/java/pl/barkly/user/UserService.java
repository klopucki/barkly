package pl.barkly.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.barkly.user.api.CurrentUserResponse;
import pl.barkly.user.api.RegisterRequest;

import java.util.Locale;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CurrentUserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (request.role() != UserRole.USER && request.role() != UserRole.SCHOOL_ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only USER and SCHOOL_ADMIN roles can be selected during registration");
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        UserEntity user = userRepository.save(new UserEntity(
                email, passwordEncoder.encode(request.password()), request.displayName().trim(), request.role()
        ));
        return CurrentUserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserEntity currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUserResponse() {
        return CurrentUserResponse.from(currentUser());
    }
}
