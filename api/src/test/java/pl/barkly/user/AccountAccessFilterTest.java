package pl.barkly.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

class AccountAccessFilterTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserEntity user =
            new UserEntity("user@example.com", "hash", "User", UserRole.SUPER_ADMIN);
    private final AccountAccessFilter filter = new AccountAccessFilter(users);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);

    private void login() {
        var principal = new AccountPrincipal(user);
        SecurityContextHolder.getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken.authenticated(
                                principal, null, principal.getAuthorities()));
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        request.getSession();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void activeUnchangedAccountCanContinue() throws Exception {
        login();
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void deactivationInvalidatesExistingSession() throws Exception {
        login();
        user.updateAccess(UserRole.SUPER_ADMIN, false);
        filter.doFilter(request, response, chain);
        assertRejected();
    }

    @Test
    void reactivationDoesNotRestoreOldSession() throws Exception {
        login();
        user.updateAccess(UserRole.SUPER_ADMIN, false);
        user.updateAccess(UserRole.SUPER_ADMIN, true);
        filter.doFilter(request, response, chain);
        assertRejected();
    }

    @Test
    void roleChangeRevokesStaleAuthorities() throws Exception {
        login();
        user.updateAccess(UserRole.USER, true);
        filter.doFilter(request, response, chain);
        assertRejected();
    }

    @Test
    void disabledAccountCannotAuthenticate() {
        user.updateAccess(UserRole.USER, false);
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        assertThat(
                        new SecurityConfig()
                                .userDetailsService(users)
                                .loadUserByUsername(user.getEmail())
                                .isEnabled())
                .isFalse();
    }

    @Test
    void anonymousRequestsContinue() throws Exception {
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verifyNoInteractions(users);
    }

    private void assertRejected() throws Exception {
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(request.getSession(false)).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(chain);
    }
}
