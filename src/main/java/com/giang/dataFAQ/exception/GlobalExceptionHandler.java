package com.giang.dataFAQ.exception;

import com.giang.dataFAQ.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRequest(InvalidRequestException ex, HttpServletRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(400, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        ex.printStackTrace(); // Cho debug
        ErrorResponseDto error = new ErrorResponseDto(500, "Lỗi hệ thống: " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.internalServerError().body(error);
    }
}
