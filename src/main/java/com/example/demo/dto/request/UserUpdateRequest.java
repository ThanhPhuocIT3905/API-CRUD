package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequest {
   // Thông tin cần thiết để cập nhật người dùng

   // Các trường dữ liệu với các ràng buộc xác thực
    private String name;

    @Email(message = "Email không hợp lệ")
    // @NotBlank(message = "Email không được để trống")
    private String email;   

    // Mật khẩu với ràng buộc xác thực  
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
             message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt")
    private String password;
}
    