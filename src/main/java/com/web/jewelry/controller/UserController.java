package com.web.jewelry.controller;

import com.web.jewelry.dto.request.UserRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.UserResponse;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.User;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse> getAllCustomer(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size){
        Page<Customer> customers = userService.getAllCustomers(PageRequest.of(page-1, size));
        Page<UserResponse> response = userService.convertToUserResponse(customers);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<ApiResponse> getCustomer(@PathVariable Long id) {
        User user = userService.getCustomerById(id);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @GetMapping("/staffs/{id}")
    public ResponseEntity<ApiResponse> getStaff(@PathVariable Long id) {
        User user = userService.getStaffById(id);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PostMapping("/add-customer")
    public ResponseEntity<ApiResponse> addUser(@RequestBody UserRequest request) {
        User user = userService.createCustomer(request);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PostMapping("/add-staff")
    public ResponseEntity<ApiResponse> addStaff(@RequestBody UserRequest request) {
        User user = userService.createStaff(request);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PutMapping("/update-customer/{id}")
    public ResponseEntity<ApiResponse> updateCustomer(@PathVariable Long id, @RequestBody UserRequest request) {
        User user = userService.updateCustomer(request, id);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PutMapping("/update-staff/{id}")
    public ResponseEntity<ApiResponse> updateStaff(@PathVariable Long id, @RequestBody UserRequest request) {
        User user = userService.updateStaff(request, id);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @DeleteMapping("/delete-staff/{id}")
    public ResponseEntity<ApiResponse> deleteStaff(@PathVariable Long id) {
        userService.deleteStaff(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/deactivate-staff/{id}")
    public ResponseEntity<ApiResponse> deactivateStaff(@PathVariable Long id) {
        userService.deactivateStaff(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/activate-staff/{id}")
    public ResponseEntity<ApiResponse> activateStaff(@PathVariable Long id) {
        userService.activateStaff(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/deactivate-customer/{id}")
    public ResponseEntity<ApiResponse> deactivateCustomer(@PathVariable Long id) {
        userService.deactivateCustomer(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @PutMapping("/activate-customer/{id}")
    public ResponseEntity<ApiResponse> activateCustomer(@PathVariable Long id) {
        userService.activateCustomer(id);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
