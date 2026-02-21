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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * User: Entity đại diện cho người dùng trong hệ thống
 * 
 * Chức năng:
 * - Lưu trữ thông tin user cơ bản (id, name, email, password)
 * - Hỗ trợ role-based authentication với enum Role
 * - Validate dữ liệu đầu vào với Jakarta Validation
 * - Tích hợp với Spring Security cho authentication
 */
@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * Tên đầy đủ của user
     */
    private String name;
    
    /**
     * Email của user - được sử dụng làm username cho authentication
     * Phải là duy nhất và không được để trống
     */
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    /**
     * Mật khẩu đã được mã hóa với BCrypt
     * Phải tuân thủ quy tắc mật khẩu mạnh
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;
    
    /**
     * Role của user trong hệ thống
     * Mặc định là USER cho user mới đăng ký
     */
    @Enumerated(EnumType.STRING) // Lưu role dưới dạng string trong database
    @Column(nullable = false)
    private Role role = Role.USER;
    
    /**
     * Enum định nghĩa các role trong hệ thống
     * Có thể mở rộng thêm các role khác như ADMIN, MODERATOR, etc.
     */
    public enum Role {
        USER,    // User thông thường - có thể truy cập các endpoint cơ bản
        ADMIN,   // Administrator - có toàn quyền
        MODERATOR // Moderator - có quyền quản lý nội dung
    }
}