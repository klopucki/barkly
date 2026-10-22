package pl.barkly.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    private static final String SUPER_ADMIN = "SUPER_ADMIN";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return email ->
                users.findByEmailIgnoreCase(email)
                        .map(user -> new AccountPrincipal(user))
                        .orElseThrow(
                                () ->
                                        new org.springframework.security.core.userdetails
                                                .UsernameNotFoundException(email));
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository users)
            throws Exception {
        return http.addFilterBefore(
                        new AccountAccessFilter(users),
                        org.springframework.security.web.access.intercept.AuthorizationFilter.class)
                .csrf(
                        csrf ->
                                csrf.csrfTokenRepository(
                                                CookieCsrfTokenRepository.withHttpOnlyFalse())
                                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                                        .ignoringRequestMatchers(
                                                "/api/auth/register", "/api/auth/login"))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(context -> context.requireExplicitSave(false))
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(
                        logout ->
                                logout.logoutUrl("/api/auth/logout")
                                        .logoutSuccessHandler(
                                                (request, response, authentication) ->
                                                        response.setStatus(204)))
                .authorizeHttpRequests(
                        requests ->
                                requests.requestMatchers("/actuator/health", "/actuator/health/**")
                                        .permitAll()
                                        .requestMatchers(
                                                "/api/auth/register",
                                                "/api/auth/login",
                                                "/api/auth/csrf")
                                        .permitAll()
                                        .requestMatchers("/api/admin/**")
                                        .hasRole(SUPER_ADMIN)
                                        .requestMatchers("/api/auth/me", "/api/my/**")
                                        .authenticated()
                                        .requestMatchers(
                                                org.springframework.http.HttpMethod.GET, "/api/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .build();
    }

    /**
     * Accepts Angular's token copied from the XSRF-TOKEN cookie while still using Spring Security's
     * XOR encoding when the token is rendered by a page.
     */
    private static final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        private final CsrfTokenRequestHandler xorHandler =
                new XorCsrfTokenRequestAttributeHandler();

        @Override
        public void handle(
                HttpServletRequest request,
                HttpServletResponse response,
                Supplier<CsrfToken> csrfToken) {
            xorHandler.handle(request, response, csrfToken);
            csrfToken.get();
        }

        @Override
        public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
            return StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))
                    ? super.resolveCsrfTokenValue(request, csrfToken)
                    : xorHandler.resolveCsrfTokenValue(request, csrfToken);
        }
    }
}
