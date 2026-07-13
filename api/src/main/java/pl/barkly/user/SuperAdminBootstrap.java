package pl.barkly.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
class SuperAdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    SuperAdminBootstrap(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${barkly.admin.email:}") String email,
            @Value("${barkly.admin.password:}") String password
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            return;
        }
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        UserEntity user = users.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> new UserEntity(normalizedEmail, passwordEncoder.encode(password), "Super admin", UserRole.SUPER_ADMIN));
        user.makeSuperAdmin();
        users.save(user);
    }
}
