package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Biểu diễn thực thể người dùng trong hệ thống
@Entity
@Data
@Table(name = "users") // Bảng lưu trữ thông tin người dùng
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    
    @Column(unique = true, nullable = false) // Email phải là duy nhất
    @NotBlank(message = "Email không được để trống") // Email không được để trống
    @Email(message = "Email không hợp lệ") // Kiểm tra định dạng email
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống") // Mật khẩu không được để trống
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;
}