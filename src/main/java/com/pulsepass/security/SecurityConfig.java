package com.pulsepass.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception { return configuration.getAuthenticationManager(); }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter, JsonAuthenticationEntryPoint entryPoint, JsonAccessDeniedHandler deniedHandler) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/venues/**", "/api/artists/**", "/api/events/published").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/by-artist", "/api/events/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/events").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/events/*/publish").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/events/*/artists/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/events/*/tickets/paid").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/tickets/*/use").hasAnyRole("STAFF", "ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
