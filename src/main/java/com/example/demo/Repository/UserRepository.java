package com.example.demo.Repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Giao diện truy cập dữ liệu cho thực thể User
    // Tìm người dùng theo email
    Optional<User> findByEmail(String email);

     // Tìm người dùng theo tên
    List<User> findByName(String name);

}
