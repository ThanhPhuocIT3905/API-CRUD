package com.example.demo.entity;

import com.example.demo.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Enhanced User entity với multi-role và basic account management
 */
@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
        message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
    )
    private String password;
    
    /**
     * Multi-role support - User có thể có nhiều role
     * Mặc định là ROLE_USER cho user mới
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();
    
    /**
     * Account status fields
     */
    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;
    
    @Column(name = "failed_attempts")
    private int failedAttempts = 0;
    
    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "enabled")
    private boolean enabled = true;
    
    /**
     * Timestamps
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    /**
     * Auto-setup khi tạo user mới
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Add default role nếu chưa có role nào
        if (roles.isEmpty()) {
            roles.add(Role.ROLE_USER);
        }
    }
    
    /**
     * Helper methods
     */
    public boolean hasRole(Role role) {
        return roles.contains(role);
    }
    
    public void addRole(Role role) {
        roles.add(role);
    }
    
    public void removeRole(Role role) {
        roles.remove(role);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    public boolean isAccountLocked() {
        return !accountNonLocked || 
               (lockTime != null && lockTime.isAfter(LocalDateTime.now()));
    }
    
    public void lockAccount(int lockDurationMinutes) {
        this.accountNonLocked = false;
        this.lockTime = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }
    
    public void unlockAccount() {
        this.accountNonLocked = true;
        this.lockTime = null;
        this.failedAttempts = 0;
    }
    
    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }
    
    public void resetFailedAttempts() {
        this.failedAttempts = 0;
    }
}