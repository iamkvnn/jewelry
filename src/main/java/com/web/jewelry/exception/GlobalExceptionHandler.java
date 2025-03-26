package com.web.jewelry.exception;

import com.web.jewelry.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = ResourceNotFoundException.class)
    ResponseEntity<ApiResponse> handlingResourceNotFoundException(ResourceNotFoundException e) {
        ApiResponse apiResponse = new ApiResponse("404", e.getMessage(), null);
        return ResponseEntity.status(404).body(apiResponse);
    }
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequestException(BadRequestException ex) {
        ApiResponse apiResponse = new ApiResponse("400", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse> handleInternalServerError(Exception ex) {
        ApiResponse apiResponse = new ApiResponse("500", "Internal Server Error: " + ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }
    @ExceptionHandler(value = NoSuchAlgorithmException.class)
    public ResponseEntity<ApiResponse> momoPaymentException(NoSuchAlgorithmException ex) {
        ApiResponse apiResponse = new ApiResponse("500", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
    @ExceptionHandler(value = InvalidKeyException.class)
    public ResponseEntity<ApiResponse> momoPaymentException(InvalidKeyException ex) {
        ApiResponse apiResponse = new ApiResponse("500", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }
}
