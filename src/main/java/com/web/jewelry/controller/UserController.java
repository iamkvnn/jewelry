package com.web.jewelry.controller;

import com.web.jewelry.dto.request.UserRequest;
import com.web.jewelry.dto.response.ApiResponse;
import com.web.jewelry.dto.response.UserResponse;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.Staff;
import com.web.jewelry.model.User;
import com.web.jewelry.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/staffs")
    public ResponseEntity<ApiResponse> getAllStaff(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size){
        Page<Staff> staffs = userService.getAllStaff(PageRequest.of(page-1, size));
        Page<UserResponse> response = userService.convertToUserResponse(staffs);
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

    @GetMapping("my-info")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        User user = userService.getCurrentUser();
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PostMapping("/add-staff")
    public ResponseEntity<ApiResponse> addStaff(@RequestBody UserRequest request) {
        User user = userService.createStaff(request);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateCustomer(@RequestBody UserRequest request) {
        User user = userService.updateCurrentUser(request);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PutMapping("/update-staff/{id}")
    public ResponseEntity<ApiResponse> updateStaff(@PathVariable Long id, @RequestBody UserRequest request) {
        User user = userService.updateStaff(request, id);
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @DeleteMapping("/customer/delete-my-account")
    public ResponseEntity<ApiResponse> deleteCurrentCustomer() {
        userService.deleteCurrentCustomer();
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
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

    @PostMapping("/register-for-news")
    public ResponseEntity<ApiResponse> registerForNews(@RequestParam boolean isSubscribed) {
        userService.setRegisterForNews(isSubscribed);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }

    @GetMapping("/search-customers")
    public ResponseEntity<ApiResponse> searchCustomers(@RequestParam String name, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int size) {
        Page<Customer> customers = userService.findCustomerByName(name, PageRequest.of(page-1, size));
        Page<UserResponse> response = userService.convertToUserResponse(customers);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @GetMapping("/search-staffs")
    public ResponseEntity<ApiResponse> searchStaffs(@RequestParam String name, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int size) {
        Page<Staff> staffs = userService.findStaffByName(name, PageRequest.of(page-1, size));
        Page<UserResponse> response = userService.convertToUserResponse(staffs);
        return ResponseEntity.ok(new ApiResponse("200", "Success", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        userService.changePassword(oldPassword, newPassword);
        return ResponseEntity.ok(new ApiResponse("200", "Success", null));
    }
}
