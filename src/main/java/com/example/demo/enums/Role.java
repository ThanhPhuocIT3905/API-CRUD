package com.example.demo.enums;
 
/**
 * Role enum định nghĩa 3 vai trò chính trong hệ thống
 * Sử dụng prefix ROLE_ để tương thích với Spring Security
 */
public enum Role {
    ROLE_USER,      // User thông thường - truy cập endpoints cơ bản
    ROLE_ADMIN,     // Administrator - toàn quyền hệ thống
    ROLE_MODERATOR  // Moderator - quản lý nội dung và user
}
    