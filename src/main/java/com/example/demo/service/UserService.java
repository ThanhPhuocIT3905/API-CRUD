package com.example.demo.service;
import org.springframework.stereotype.Service;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.entity.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

// Dịch vụ quản lý người dùng
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository; // Truy cập dữ liệu người dùng
    @Autowired
    private PasswordEncoder passwordEncoder; // Mã hóa mật khẩu

    // Tạo người dùng mới (CREATE)
    public User createUser(UserCreationRequest request) {
        User user = new User(); 
        user.setName(request.getName());

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        user.setEmail(request.getEmail());


        user.setPassword(passwordEncoder.encode(request.getPassword())); // Mã hóa mật khẩu
        return userRepository.save(user);
    }

    // Lấy danh sách người dùng (READ)
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    // Lấy thông tin người dùng theo ID (READ)
    public User getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Lay thông tin người dùng theo email (READ)
    public User searchByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));
    }

    // Lấy thông tin người dùng theo tên (READ)
    public List<User> searchByName(String name) {
        List<User> users = userRepository.findByName(name);
        if (users.isEmpty()) {
            throw new RuntimeException("Không tìm thấy user với tên: " + name);
        }
        return users;
    }

    // Cập nhật thông tin người dùng (UPDATE)
    public User updateUser(String userId, UserUpdateRequest request) {
        // Tìm người dùng theo ID
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Kiểm tra và cập nhật name nếu có
        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) {
                throw new RuntimeException("Tên không được để trống");
            }
            user.setName(request.getName());
        }

        // Kiểm tra và cập nhật email nếu có
        if (request.getEmail() != null) {
            if (request.getEmail().trim().isEmpty()) {
                throw new RuntimeException("Email không được để trống");
            }
            if (!request.getEmail().equals(user.getEmail()) &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã tồn tại");
            }
            user.setEmail(request.getEmail());
        }

        // Cập nhật mật khẩu nếu có thay đổi
        if (request.getPassword() != null) {
            if (request.getPassword().trim().isEmpty()) {
                throw new RuntimeException("Mật khẩu không được để trống");
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    // Xóa người dùng (DELETE)
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }
}
