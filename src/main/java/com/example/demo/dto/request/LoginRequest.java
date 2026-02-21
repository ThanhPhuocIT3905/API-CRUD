package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LoginRequest: DTO cho request đăng nhập
 * 
 * Chức năng:
 * - Chứa thông tin đăng nhập từ client
 * - Validate dữ liệu đầu vào
 * - Được sử dụng trong AuthController cho endpoint login
 */
@Data
public class LoginRequest {
    
    /**
     * Email của user (sử dụng làm username)
     * Validation: Không được để trống và phải là email hợp lệ
     */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    /**
     * Mật khẩu của user
     * Validation: Không được để trống
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}