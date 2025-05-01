package com.web.jewelry.exception;

import com.web.jewelry.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = AlreadyExistException.class)
    public ResponseEntity<ApiResponse> handleAlreadyExistException(AlreadyExistException ex) {
        ApiResponse apiResponse = new ApiResponse("409", ex.getMessage(), null);
        return ResponseEntity.ok(apiResponse);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    ResponseEntity<ApiResponse> handlingResourceNotFoundException(ResourceNotFoundException e) {
        ApiResponse apiResponse = new ApiResponse("1000", e.getMessage(), null);
        return ResponseEntity.ok(apiResponse);
    }
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequestException(BadRequestException ex) {
        ApiResponse apiResponse = new ApiResponse("400", ex.getMessage(), null);
        return ResponseEntity.ok(apiResponse);
    }
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse> handleInternalServerError(Exception ex) {
        ApiResponse apiResponse = new ApiResponse("500", "Internal Server Error ", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse> handleAuthorizationDeniedException() {
        ApiResponse apiResponse = new ApiResponse("403", "Access denied", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        ApiResponse apiResponse = new ApiResponse("404", "Resource not found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
    }
}
