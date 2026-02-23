package com.example.demo.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // Xử lý các exception global
public class GlobalExceptionHandler {

    // Xử lý exception khi validate dữ liệu
    @ExceptionHandler(MethodArgumentNotValidException.class) // Xử lý exception khi validate dữ liệu
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>(); // Lưu trữ thông tin lỗi
        
        // Lấy thông tin lỗi từ exception
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField(); // Lấy tên trường
            String errorMessage = error.getDefaultMessage(); // Lấy thông báo lỗi
            errors.put(fieldName, errorMessage); // Lưu thông tin lỗi
        });
        
        // Tạo response
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Lỗi xác thực"); // Thêm thông tin lỗi
        response.put("message", "Dữ liệu không hợp lệ"); // Thêm thông báo lỗi
        response.put("errors", errors); // Lấy thông tin lỗi
        response.put("timestamp", java.time.LocalDateTime.now().toString()); // Thêm thời gian
        // Trả về response
        return ResponseEntity.badRequest().body(response);
    }

    // Xử lý exception khi validate dữ liệu
    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<Map<String, Object>> handleFieldValidationException(FieldValidationException ex) {
        Map<String, Object> response = new HashMap<>(); // Lưu trữ thông tin lỗi
        response.put("error", "Lỗi xác thực"); // Thêm thông tin lỗi
        response.put("message", "Dữ liệu không hợp lệ"); // Thêm thông báo lỗi
        response.put("errors", ex.getErrors()); // Lấy thông tin lỗi
        response.put("timestamp", java.time.LocalDateTime.now().toString()); // Thêm thời gian
        // Trả về response
        return ResponseEntity.badRequest().body(response);
    }
}
