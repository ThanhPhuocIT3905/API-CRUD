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
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

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
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Tạo response với token và thông tin user
            JwtResponse jwtResponse = new JwtResponse(jwt, user.getId(), user.getEmail(), user.getName(), jwt);

            return ResponseEntity.ok(jwtResponse);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed");
            errorResponse.put("message", "Invalid email or password");
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
                errorResponse.put("error", "Email already exists");
                errorResponse.put("message", "Email is already registered");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Kiểm tra xác nhận mật khẩu
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Password mismatch");
                errorResponse.put("message", "Password and confirm password do not match");
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
            response.put("message", "User registered successfully");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            response.put("name", savedUser.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
