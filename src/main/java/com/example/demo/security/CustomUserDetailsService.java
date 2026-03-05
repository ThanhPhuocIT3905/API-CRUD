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
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm user trong database bằng email
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Convert User entity thành UserDetails object
        return org.springframework.security.core.userdetails.User.builder() 
                .username(user.getEmail()) // Sử dụng email làm username
                .password(user.getPassword()) // Sử dụng password
                .authorities(getUserAuthorities(user)) // Lấy authorities từ user
                .accountExpired(!user.isAccountNonExpired()) // Hết hạn
                .accountLocked(!user.isAccountNonLocked()) // Bị khóa
                .credentialsExpired(!user.isCredentialsNonExpired()) // Hết hạn
                .disabled(!user.isEnabled()) // Bị vô hiệu hóa
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
        
        // Multi-role support - lặp qua tất cả roles
        for (com.example.demo.enums.Role role : user.getRoles()) {
            // Thêm role với prefix "ROLE_"
            authorities.add(new SimpleGrantedAuthority(role.name()));
            
            // Thêm permissions dựa trên từng role
            switch (role) {
                // Admin có tất cả quyền
                case ROLE_ADMIN:
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_ALL"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_WRITE_ALL"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_DELETE_ALL"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_USERS"));
                    break;
                    //Moderator có quyền đọc, viết, và moderating
                case ROLE_MODERATOR:
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_ALL"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_WRITE_CONTENT"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MODERATE"));
                    break;
                    // User có quyền đọc và viết cho bản thân
                case ROLE_USER:
                default:
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_OWN"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_WRITE_OWN"));
                    break;
            }
        }
        
        return authorities;
    }
}