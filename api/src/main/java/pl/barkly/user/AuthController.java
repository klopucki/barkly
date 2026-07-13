package pl.barkly.user;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.barkly.user.api.CurrentUserResponse;
import pl.barkly.user.api.RegisterRequest;
import pl.barkly.user.api.LoginRequest;

@RestController
class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/api/auth/register")
    CurrentUserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/api/auth/login")
    CurrentUserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        try {
            var authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
            servletRequest.getSession();
            servletRequest.changeSessionId();
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            return userService.currentUserResponse();
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    @GetMapping("/api/auth/csrf")
    CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/api/auth/me")
    CurrentUserResponse currentUser() {
        return userService.currentUserResponse();
    }
}
