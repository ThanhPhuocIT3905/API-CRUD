package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CustomUserDetailsService: Implementation của UserDetailsService cho Spring Security
 * 
 * Chức năng chính:
 * - Load user từ database bằng username (email trong trường hợp này)
 * - Convert User entity thành UserDetails object
 * - Cung cấp authorities/roles cho user
 * - Được sử dụng bởi Spring Security để authenticate users
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by username (email trong trường hợp này)
     * Method này được Spring Security gọi trong quá trình authentication
     * 
     * @param username Email của user (sử dụng email làm username)
     * @return UserDetails object chứa thông tin user
     * @throws UsernameNotFoundException Nếu không tìm thấy user
     */
    @Override
    @Transactional(readOnly = true) // Read-only transaction để tối ưu performance
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm user trong database bằng email
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Convert User entity thành UserDetails object
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // Sử dụng email làm username
                .password(user.getPassword()) // Password đã được mã hóa BCrypt
                .authorities(getUserAuthorities(user)) // Set authorities/roles
                .accountExpired(false) // Account không hết hạn
                .accountLocked(false) // Account không bị khóa
                .credentialsExpired(false) // Credentials không hết hạn
                .disabled(false) // Account không bị disabled
                .build();
    }

    /**
     * Lấy authorities/roles của user
     * Support multiple roles dựa trên User.Role enum
     * 
     * @param user User entity
     * @return Collection của SimpleGrantedAuthority
     */
    private java.util.Collection<SimpleGrantedAuthority> getUserAuthorities(User user) {
        java.util.ArrayList<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
        
        // Thêm role của user với prefix "ROLE_"
        // Spring Security yêu cầu role phải có prefix "ROLE_"
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        
        // Có thể mở rộng để thêm các quyền khác dựa trên role
        switch (user.getRole()) {
            case ADMIN:
                // Admin có tất cả các quyền
                authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_WRITE_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_DELETE_ALL"));
                break;
            case MODERATOR:
                // Moderator có quyền quản lý nội dung
                authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_WRITE_CONTENT"));
                break;
            case USER:
                // User chỉ có quyền cơ bản
                authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_OWN"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_WRITE_OWN"));
                break;
        }
        
        return authorities;
    }
}