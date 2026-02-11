package com.example.demo.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Cấu hình bảo mật cho ứng dụng
@EnableWebSecurity // Kích hoạt bảo mật web của Spring Security
public class SecurityConfig {
    @Bean // Định nghĩa bean mã hóa mật khẩu
    public PasswordEncoder passwordEncoder() { // Sử dụng BCrypt để mã hóa mật khẩu
        return new BCryptPasswordEncoder(); // Trả về đối tượng PasswordEncoder
    }
    @Bean // Cấu hình chuỗi bộ lọc bảo mật cho ứng dụng
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { // Cấu hình bảo mật HTTP
        http
            .csrf(csrf -> csrf.disable()) // Vô hiệu hóa CSRF cho API
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // Cho phép tất cả các request không cần authentication
            );
        return http.build(); // Xây dựng và trả về chuỗi bộ lọc bảo mật
    }
}
