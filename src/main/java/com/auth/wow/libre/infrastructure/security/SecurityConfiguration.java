package com.auth.wow.libre.infrastructure.security;

import com.auth.wow.libre.application.services.jwt.*;
import com.auth.wow.libre.domain.model.security.*;
import com.auth.wow.libre.domain.ports.in.jwt.*;
import com.auth.wow.libre.infrastructure.filter.AuthenticationFilter;
import com.auth.wow.libre.infrastructure.filter.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.http.*;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.*;
import org.springframework.web.cors.*;

import java.util.*;

import static com.auth.wow.libre.domain.model.constant.Constants.*;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceCustom userDetailsServiceCustom;
    private final JwtPort jwtPort;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
                                 UserDetailsServiceCustom userDetailsServiceCustom,
                                 JwtPortService jwtPort) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsServiceCustom = userDetailsServiceCustom;
        this.jwtPort = jwtPort;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("http://localhost:3000",
                "http://127.0.0.1:3000", "http://localhost:3001", "http://127.0.0.1:3001","https://api.wowlibre.com"));
        corsConfiguration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.OPTIONS.name(),
                HttpMethod.DELETE.name()
        ));
        corsConfiguration.setAllowedHeaders(Arrays.asList(
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.AUTHORIZATION,
                HEADER_TRANSACTION_ID
        ));
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(
                        endpoints -> endpoints.requestMatchers("/api/auth/login").authenticated()
                ).addFilterBefore(new AuthenticationFilter(authenticationProvider(), jwtPort),
                        UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                // INTERNAL API (siguen sin autenticación)
                                "/api/client",
                                "/api/account/create",

                                // SWAGGER (siguen sin autenticación)
                                "/v2/api-docs", "/swagger-resources",
                                "/swagger-resources/**", "/configuration/ui",
                                "/configuration/security", "/swagger-ui.html", "/webjars/**",
                                "/v3/api-docs/**", "/swagger-ui/**",

                                // PERMITIR THYMELEAF (agregado)
                                "/", "/home",  "/error","/register","/congrats",

                                // PERMITIR ARCHIVOS ESTÁTICOS (CSS, JS, IMAGES)
                                "/css/**", "/js/**", "/images/**", "/webjars/**","/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsServiceCustom);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

}
