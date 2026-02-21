package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JwtResponse: DTO cho response chứa JWT token
 * 
 * Chức năng:
 * - Chứa JWT token trả về cho client sau khi đăng nhập thành công
 * - Có thể mở rộng để chứa thêm thông tin user, refresh token, etc.
 * - Được sử dụng trong AuthController cho response format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    /**
     * JWT token được tạo sau khi authentication thành công
     * Client sẽ sử dụng token này cho các request tiếp theo
     * Format: "Bearer <token>" trong Authorization header
     */
    private String token;
    
    /**
     * Type của token (luôn là "Bearer" cho JWT)
     */
    private String type = "Bearer";
    
    /**
     * ID của user
     */
    private String id;
    
    /**
     * Email của user (username)
     */
    private String email;
    
    /**
     * Tên của user
     */
    private String name;
}
