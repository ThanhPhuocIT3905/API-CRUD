package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * UserController: Controller cho các operations liên quan đến user
 * 
 * Các endpoint được bảo vệ bởi JWT authentication:
 * - GET /api/user/profile - Lấy thông tin user hiện tại (cần authentication)
 * - GET /api/user/all - Lấy tất cả users (chỉ admin)
 * - GET /api/user/protected - Endpoint cần authentication
 * - GET /api/user/admin - Endpoint chỉ dành cho admin
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy thông tin profile của user đang đăng nhập
     * Endpoint: GET /api/user/profile
     * Security: Cần JWT token hợp lệ
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> getUserProfile() {
        // Lấy authentication từ SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Lấy email từ authentication
        String email = authentication.getName();
        
        // Tìm user trong database
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Tạo response (không bao gồm password)
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        response.put("message", "Truy xuất hồ sơ thành công");

        return response;
    }

    /**
     * Endpoint public để test - không cần authentication
     * Endpoint: GET /api/user/public
     * Security: Public endpoint
     */
    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đây là một endpoint public - không cần xác thực");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return response;
    }

    /**
     * Endpoint cho user đã đăng nhập
     * Endpoint: GET /api/user/protected
     * Security: Cần JWT token hợp lệ
     */
    @GetMapping("/protected")
    @PreAuthorize("isAuthenticated()") // Chỉ cho phép người dùng đã xác thực
    public Map<String, Object> protectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy authentication từ SecurityContext
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đây là một endpoint được bảo vệ - cần xác thực"); // Trả về thông báo
        response.put("user", authentication.getName()); // Trả về tên người dùng
        response.put("authorities", authentication.getAuthorities()); // Trả về quyền hạn
        response.put("timestamp", java.time.LocalDateTime.now().toString()); // Trả về thời gian
        
        return response;
    }

    /**
     * Endpoint chỉ dành cho admin
     * Endpoint: GET /api/user/admin
     * Security: Chỉ có role ADMIN
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đây là một endpoint chỉ dành cho admin");
        response.put("admin", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return response;
    }
}