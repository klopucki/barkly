package pl.barkly.user;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

final class AccountPrincipal extends User {
    private final long securityVersion;

    AccountPrincipal(UserEntity user) {
        super(
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        securityVersion = user.getSecurityVersion();
    }

    long securityVersion() {
        return securityVersion;
    }
}
