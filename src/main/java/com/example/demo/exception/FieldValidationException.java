package com.example.demo.exception;

import java.util.Map;

public class FieldValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public FieldValidationException(Map<String, String> errors) {
        this.errors = errors; // Lưu trữ lỗi xác thực đầu vào dưới dạng key là tên trường và value là thông báo lỗi
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}