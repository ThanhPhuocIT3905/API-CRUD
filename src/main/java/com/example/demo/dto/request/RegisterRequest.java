package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RegisterRequest: DTO cho request đăng ký tài khoản
 * 
 * Chức năng:
 * - Chứa thông tin đăng ký từ client
 * - Validate dữ liệu đầu vào theo các quy tắc bảo mật
 * - Được sử dụng trong AuthController cho endpoint register
 */
@Data
public class RegisterRequest {
    
    /**
     * Tên của user
     * Validation: Không được để trống, độ dài 2-50 ký tự
     */
    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 50, message = "Tên phải từ 2 đến 50 ký tự")
    private String name;
    
    /**
     * Email của user (sẽ được sử dụng làm username)
     * Validation: Không được để trống, phải là email hợp lệ
     */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    /**
     * Mật khẩu của user
     * Validation: Phải tuân thủ quy tắc mật khẩu mạnh
     * - Ít nhất 8 ký tự
     * - Chứa chữ hoa, chữ thường, số và ký tự đặc biệt
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;
    
    /**
     * Xác nhận mật khẩu
     * Validation: Phải khớp với mật khẩu
     */
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
