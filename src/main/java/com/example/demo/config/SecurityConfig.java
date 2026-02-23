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
import org.springframework.web.cors.CorsConfiguration; 
import org.springframework.web.cors.CorsConfigurationSource; 
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; 

import java.util.Arrays;

/**
 * SecurityConfig: Cấu hình bảo mật cho ứng dụng với JWT authentication
 * 
 * Chức năng chính:
 * - Cấu hình JWT authentication filter
 * - Set authentication manager và user details service
 * - Cấu hình các endpoint public và protected
 * - Disable session management (stateless JWT  )
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
     * Cấu hình CORS
     * Chỉ cho phép các origin cụ thể trong môi trường production
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Trong production, thay thế "*" bằng các domain cụ thể của frontend
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://yourfrontend.com"  )); // Chỉ cho phép các domain cụ thể
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Chỉ cho phép các method cụ thể
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept")); // Chỉ cho phép các header cụ thể
        configuration.setAllowCredentials(true); // Cho phép gửi cookies, authorization headers
        configuration.setMaxAge(3600L); // Thời gian cache preflight request
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Cấu hình Security Filter Chain
     * Đây là phần quan trọng nhất cho JWT authentication
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http  ) throws Exception {
        http
            // Kích hoạt CORS với cấu hình đã định nghĩa
            .cors(cors -> cors.configurationSource(corsConfigurationSource(  )))
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
                .requestMatchers("/api/user/public").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/").permitAll()
                
                // Các endpoint cần authentication
                // .requestMatchers("/api/user/**").authenticated() // Có thể bỏ nếu dùng @PreAuthorize
                // .requestMatchers("/api/admin/**").hasRole("ADMIN") // Có thể bỏ nếu dùng @PreAuthorize
                
                // Mặc định các request khác cần authentication
                .anyRequest().authenticated()
            )
            
            // Cấu hình exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Vui lòng đăng nhập để truy cập tài nguyên\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Bạn không có quyền truy cập vào tài nguyên này.\"}");
                })
            );

        return http.build();
    }
}