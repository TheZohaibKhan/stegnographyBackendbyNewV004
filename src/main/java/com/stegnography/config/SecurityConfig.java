package com.stegnography.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomLoginSuccessHandler successHandler;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          CustomLoginSuccessHandler successHandler,
                          PasswordEncoder passwordEncoder) {
        this.customUserDetailsService = customUserDetailsService;
        this.successHandler = successHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // ── Public pages & static assets ──────────────────────────────
                .requestMatchers(
                        "/login",
                        "/login.html",
                        "/login-light.html",           // ← THEME SWITCH FIX
                        "/register",
                        "/register.html",
                        "/register-light.html",        // ← THEME SWITCH FIX
                        "/forgot-password.html",
                        "/forgot-password-light.html", // ← THEME SWITCH FIX
                        "/403.html",                   // ← THEME SWITCH FIX
                        "/403-light.html",             // ← THEME SWITCH FIX
                        "/send-otp",
                        "/verify-otp",
                        "/forgot-password/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/user/profile/pic/**"
                ).permitAll()

                // ── Admin page + ALL admin APIs → ADMIN role only ──────────────
                .requestMatchers("/admin.html", "/admin-light.html").hasRole("ADMIN") // ← THEME SWITCH FIX
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // ── Regular user APIs → must be logged in ──────────────────────
                .requestMatchers("/api/**").authenticated()

                // ── Everything else → must be logged in ───────────────────────
                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/login")
                .successHandler(successHandler)
                .failureUrl("/login.html?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.html?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        http.authenticationProvider(authenticationProvider());

       // ADD THIS BLOCK ↓
        http.exceptionHandling(ex -> ex
            .accessDeniedPage("/403.html")
        );
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}