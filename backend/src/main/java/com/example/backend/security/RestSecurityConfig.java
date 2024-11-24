package com.example.backend.security;

import com.example.backend.security.jwt.JwtRequestFilter;
import com.example.backend.security.jwt.UnauthorizedHandlerJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class RestSecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    public RepositoryUserDetailService userDetailService;

    @Autowired
    private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        http
                .securityMatcher("/api/v1/**")
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(unauthorizedHandlerJwt)
                );

        http
                .authorizeHttpRequests(authorize -> authorize
                        // PRIVATE ENDPOINTS ----------------------------

                        // session endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/logout").hasAnyRole("USER")

                        // community & ban endpoints
                        .requestMatchers(HttpMethod.PUT, "/api/v1/communities/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/communities/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/communities/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/bans/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/bans/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/bans/**").hasAnyRole("USER")

                        // post endpoints
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/**").hasAnyRole("USER")

                        // reply endpoints
                        .requestMatchers(HttpMethod.PUT, "/api/v1/replies/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/replies/**").hasAnyRole("USER")

                        // user endpoints
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasAnyRole("USER")

                        // PUBLIC ENDPOINTS ----------------------------
                        .anyRequest().permitAll()
                );

        // Disable Form login Authentication
        http.formLogin(formLogin -> formLogin.disable());

        // Disable CSRF protection (it is difficult to implement in REST APIs)
        http.csrf(csrf -> csrf.disable());

        // Disable Basic Authentication
        http.httpBasic(httpBasic -> httpBasic.disable());

        // Stateless session
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Add JWT filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
