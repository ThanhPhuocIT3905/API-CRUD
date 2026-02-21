package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

/**
 * SecurityConfig: Cấu hình bảo mật cho ứng dụng với JWT authentication
 * 
 * Chức năng chính:
 * - Cấu hình JWT authentication filter
 * - Set authentication manager và user details service
 * - Cấu hình các endpoint public và protected
 * - Disable session management (stateless JWT)
 * - Cấu hình CORS và CSRF
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Cấu hình Password Encoder
     * Sử dụng BCrypt để mã hóa mật khẩu
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cấu hình Authentication Manager
     * Được sử dụng để authenticate users trong AuthController
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Cấu hình Security Filter Chain
     * Đây là phần quan trọng nhất cho JWT authentication
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF vì JWT không cần
            .csrf(csrf -> csrf.disable())
            
            // Cấu hình session management thành stateless cho JWT
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Thêm JWT filter trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Cấu hình authorization cho các endpoint
            .authorizeHttpRequests(auth -> auth
                // Các endpoint public - không cần authentication
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/").permitAll()
                
                // Các endpoint cần authentication
                .requestMatchers("/api/user/**").authenticated()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Mặc định các request khác cần authentication
                .anyRequest().authenticated()
            )
            
            // Cấu hình exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Please authenticate to access this resource\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"You don't have permission to access this resource\"}");
                })
            );

        return http.build();
    }
}