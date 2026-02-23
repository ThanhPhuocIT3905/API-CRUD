package com.example.demo.controller;

import com.example.demo.dto.response.JwtResponse;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthController: Controller xử lý authentication operations
 * 
 * Endpoints:
 * - POST /api/auth/login - Đăng nhập và tạo JWT token
 * - POST /api/auth/register - Đăng ký tài khoản mới
 * - GET /api/auth/me - Lấy thông tin user hiện tại
 * 
 * Chức năng chính:
 * - Xử lý đăng nhập và tạo JWT token
 * - Đăng ký user mới với password mã hóa
 * - Validate input và xử lý lỗi
 */
@RestController
@RequestMapping("/api/auth") // Base URL cho tất cả endpoints trong controller
@CrossOrigin(origins = {"http://localhost:3000", "https://yourfrontend.com"}, maxAge = 3600) // Cho phép CORS requests từ bất kỳ origin nào
public class AuthController {
    // Khai báo logger
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager; // Quản lý authentication

    @Autowired
    UserRepository userRepository; // Repository để truy vấn user

    @Autowired
    PasswordEncoder passwordEncoder; // Mã hóa password

    @Autowired
    JwtTokenProvider tokenProvider; // Tạo JWT token

    /**
     * Endpoint đăng nhập
     * URL: POST /api/auth/login
     * 
     * @param loginRequest Chứa email và password
     * @return JWT response với token và thông tin user
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Tạo authentication token từ email và password
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Set authentication vào SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Tạo JWT token
            String jwt = tokenProvider.generateToken(authentication);

            // Lấy thông tin user từ database
            User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Người dùng không tìm thấy"));

            // Tạo response với token và thông tin user
            JwtResponse jwtResponse = new JwtResponse(jwt, "Bearer", user.getId(), user.getEmail(), user.getName());

            return ResponseEntity.ok(jwtResponse);
            
        } catch (Exception e) {
            logger.error("Authentication failed for user {}: {}", loginRequest.getEmail(), e.getMessage(), e); // Ghi log chi tiết
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Đăng nhập thất bại");
            errorResponse.put("message", "Email hoặc mật khẩu không đúng");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Endpoint đăng ký tài khoản mới
     * URL: POST /api/auth/register
     * 
     * @param registerRequest Chứa thông tin đăng ký
     * @return Success message hoặc error
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Kiểm tra email đã tồn tại chưa
            if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Email đã tồn tại");
                errorResponse.put("message", "Email đã được đăng ký");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Kiểm tra xác nhận mật khẩu
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Mật khẩu không khớp");
                errorResponse.put("message", "Mật khẩu và xác nhận mật khẩu không khớp");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Tạo user mới
            User user = new User();
            user.setName(registerRequest.getName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

            // Lưu user vào database
            User savedUser = userRepository.save(user);

            // Tạo response success
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký tài khoản thành công");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            response.put("name", savedUser.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Registration failed for user {}: {}", registerRequest.getEmail(), e.getMessage(), e); // Ghi log chi tiết
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Đăng ký thất bại");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
