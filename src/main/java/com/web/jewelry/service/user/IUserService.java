package com.web.jewelry.service.user;

import com.web.jewelry.dto.request.UserRequest;
import com.web.jewelry.dto.response.UserResponse;
import com.web.jewelry.model.User;

public interface IUserService {
    User createCustomer(UserRequest request);
    User createStaff(UserRequest request);
    User updateCustomer(UserRequest request, Long id);
    User updateStaff(UserRequest request, Long id);

    User getStaffById(Long id);
    User getCustomerById(Long id);

    void deactivateStaff(Long id);
    void activateStaff(Long id);
    void deactivateCustomer(Long id);
    void activateCustomer(Long id);

    void deleteStaff(Long id);

    User getCurrentUser();
    User updateCurrentUser(UserRequest request);
    User deleteCurrentUser();

    UserResponse convertToUserResponse(User user);
}
