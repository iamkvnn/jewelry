package com.web.jewelry.service.user;

import com.web.jewelry.dto.request.ResetPasswordRequest;
import com.web.jewelry.dto.request.UserRequest;
import com.web.jewelry.dto.response.UserResponse;
import com.web.jewelry.enums.EUserRole;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.Manager;
import com.web.jewelry.model.Staff;
import com.web.jewelry.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface IUserService {
    Page<Staff> getAllStaff(Pageable pageable);
    Page<Customer> getAllCustomers(Pageable pageable);
    Page<Manager> getAllManagers(Pageable pageable);

    Set<Long> getAllUerRegisteredForNews();

    Page<Customer> findCustomerByName(String name, Pageable pageable);
    Page<Staff> findStaffByName(String name, Pageable pageable);

    void sendEmailResetPassword(String email, EUserRole role);
    void changePassword(String oldPassword, String newPassword);
    void resetPassword(ResetPasswordRequest request);
    void setRegisterForNews(boolean isSubscribed);

    User createCustomer(UserRequest request);
    User createStaff(UserRequest request);

    User updateStaff(UserRequest request, Long id);

    User getStaffById(Long id);
    User getCustomerById(Long id);

    User getCustomerByEmail(String username);
    User getManagerByEmail(String username);
    User getStaffByEmail(String username);

    void deactivateStaff(Long id);
    void activateStaff(Long id);
    void deactivateCustomer(Long id);
    void activateCustomer(Long id);

    void deleteStaff(Long id);

    User getCurrentUser();
    User updateCurrentUser(UserRequest request);
    void sendRequestDeleteCurrentCustomer();

    <T> UserResponse convertToUserResponse(T user);
    <T> Page<UserResponse> convertToUserResponse(Page<T> users);

    void confirmDeleteCurrentCustomer(String token);
}
