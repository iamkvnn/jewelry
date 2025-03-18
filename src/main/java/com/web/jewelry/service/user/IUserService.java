package com.web.jewelry.service.user;

import com.web.jewelry.dto.request.UserRequest;
import com.web.jewelry.dto.response.UserResponse;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.Manager;
import com.web.jewelry.model.Staff;
import com.web.jewelry.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    Page<Staff> getAllStaff(Pageable pageable);
    Page<Customer> getAllCustomers(Pageable pageable);
    Page<Manager> getAllManagers(Pageable pageable);

    User createCustomer(UserRequest request);
    User createStaff(UserRequest request);

    User updateCustomer(UserRequest request, Long id);
    User updateStaff(UserRequest request, Long id);
    User updateManager(UserRequest request, Long id);

    User getStaffById(Long id);
    User getCustomerById(Long id);
    User getManagerById(Long id);

    User getCustomerByUsername(String username);
    User getManagerByUsername(String username);
    User getStaffByUsername(String username);

    void deactivateStaff(Long id);
    void activateStaff(Long id);
    void deactivateCustomer(Long id);
    void activateCustomer(Long id);

    void deleteStaff(Long id);

    User getCurrentUser();
    User updateCurrentUser(UserRequest request);
    User deleteCurrentUser();

    <T> UserResponse convertToUserResponse(T user);
    <T> Page<UserResponse> convertToUserResponse(Page<T> users);

}
