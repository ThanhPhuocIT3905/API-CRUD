package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.example.demo.service.UserService;

import jakarta.validation.Valid;

import com.example.demo.entity.User;
import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdateRequest;

@RestController
@RequestMapping("/users")
public class UserController {
    // Inject UserService để xử lý logic nghiệp vụ
    @Autowired
    private UserService userService;

    // Lấy danh sách người dùng (READ)
    @GetMapping
    List<User> getUsers() {
        return userService.getUsers();
    }

    // Lấy thông tin người dùng theo ID (READ)
    @GetMapping("/{userId}")
    User getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    // Lấy thông tin người dùng theo email (READ)
    @GetMapping("/searchEmail")
    public User searchByEmail(@RequestParam String email) {
        return userService.searchByEmail(email);
    }

    // Lấy thông tin người dùng theo tên (READ)
    @GetMapping("/searchName")
    public List<User> searchByName(@RequestParam String name) {
        return userService.searchByName(name);
    }

    // Tạo người dùng mới (CREATE)
    @PostMapping
    User createUser(@RequestBody @Valid UserCreationRequest request) {
        return userService.createUser(request);  
    }

    // Cập nhật thông tin người dùng (UPDATE)
    @PutMapping("/{userId}")
    User updateUser(@PathVariable String userId, @RequestBody @Valid UserUpdateRequest request) {
        return userService.updateUser(userId, request);
    }

    // Xóa người dùng (DELETE)
    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return "User deleted successfully";
    }

    // Xử lý lỗi xác thực đầu vào và lỗi nghiệp vụ (ví dụ: email đã được sử dụng)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400 Bad Request khi có lỗi xác thực hoặc lỗi nghiệp vụ
    @ExceptionHandler({MethodArgumentNotValidException.class, RuntimeException.class}) // Bắt cả lỗi xác thực và lỗi nghiệp vụ
    public Map<String, String> handleValidationAndBusinessExceptions(Exception ex) { // Trả về lỗi xác thực và lỗi nghiệp vụ dưới dạng JSON với key là tên trường và value là thông báo lỗi
        Map<String, String> errors = new HashMap<>(); // Lưu trữ lỗi xác thực và lỗi nghiệp vụ
        if (ex instanceof MethodArgumentNotValidException manvEx) { // Kiem tra lỗi xác thực
            manvEx.getBindingResult().getAllErrors().forEach((error) -> { // Lấy tất cả lỗi xác thực và lưu vào map errors
                String fieldName = ((FieldError) error).getField(); // Lấy tên trường gây lỗi
                String errorMessage = error.getDefaultMessage(); // Lấy thông báo lỗi
                errors.put(fieldName, errorMessage); // Lưu lỗi vào map với key là tên trường và value là thông báo lỗi
            });
        } else {
            errors.put("error", ex.getMessage()); // Lưu lỗi nghiệp vụ vào map với key là "error" và value là thông báo lỗi
        }
        return errors; // Trả về lỗi xác thực và lỗi nghiệp vụ dưới dạng JSON với key là tên trường và value là thông báo lỗi
    }
}