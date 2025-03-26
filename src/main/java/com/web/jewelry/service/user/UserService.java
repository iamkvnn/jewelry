package com.web.jewelry.service.user;

import com.web.jewelry.dto.request.UserRequest;
import com.web.jewelry.dto.response.UserResponse;
import com.web.jewelry.enums.EMembershiprank;
import com.web.jewelry.enums.EUserRole;
import com.web.jewelry.enums.EUserStatus;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.BadRequestException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.Manager;
import com.web.jewelry.model.Staff;
import com.web.jewelry.model.User;
import com.web.jewelry.repository.CustomerRepository;
import com.web.jewelry.repository.ManagerRepository;
import com.web.jewelry.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService {
    private final StaffRepository staffRepository;
    private final ModelMapper modelMapper;
    private final CustomerRepository customerRepository;
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<Staff> getAllStaff(Pageable pageable) {
        return staffRepository.findAll(pageable);
    }

    @Override
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Override
    public Page<Manager> getAllManagers(Pageable pageable) {
        return managerRepository.findAll(pageable);
    }

    @Override
    public User createStaff(UserRequest request) {
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistException("Email already exists");
        }
        return staffRepository.save(Staff.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .dob(request.getDob())
                .gender(request.getGender())
                .status(EUserStatus.ACTIVE)
                .role(EUserRole.STAFF)
                .joinAt(LocalDateTime.now())
                .build()
        );
    }

    @Override
    public User createCustomer(UserRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistException("Email already exists");
        }
        return customerRepository.save(Customer.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .dob(request.getDob())
                .gender(request.getGender())
                .totalSpent(0L)
                .membershipRank(EMembershiprank.MEMBER)
                .isSubscribedForNews(false)
                .status(EUserStatus.ACTIVE)
                .role(EUserRole.CUSTOMER)
                .joinAt(LocalDateTime.now())
                .build()
        );
    }

    @Override
    public User getStaffById(Long id) {
        return staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    }

    @Override
    public User getCustomerById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Override
    public User getManagerById(Long id) {
        return managerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
    }

    @Override
    public User getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Override
    public User getManagerByEmail(String email) {
        return managerRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
    }

    @Override
    public User getStaffByEmail(String email) {
        return staffRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    }

    @Override
    public User updateStaff(UserRequest request, Long id) {
        return staffRepository.findById(id)
                .map(staff -> {
                    staff.setPhone(request.getPhone());
                    staff.setFullName(request.getFullName());
                    staff.setDob(request.getDob());
                    staff.setGender(request.getGender());
                    return staffRepository.save(staff);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    }

    @Override
    public void deleteStaff(Long id) {
        staffRepository.findById(id).ifPresentOrElse(staffRepository::delete, () -> {
            throw new ResourceNotFoundException("Staff not found");
        });
    }

    @Override
    public void deactivateStaff(Long id){
        Staff staff = staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        staff.setStatus(EUserStatus.BANNED);
        staffRepository.save(staff);
    }

    @Override
    public void activateStaff(Long id){
        Staff staff = staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
        staff.setStatus(EUserStatus.ACTIVE);
        staffRepository.save(staff);
    }

    @Override
    public void deactivateCustomer(Long id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.setStatus(EUserStatus.BANNED);
        customerRepository.save(customer);
    }

    @Override
    public void activateCustomer(Long id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.setStatus(EUserStatus.ACTIVE);
        customerRepository.save(customer);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        String scope = authentication.getAuthorities().stream().findFirst().orElseThrow(() -> new BadRequestException("Invalid scope")).getAuthority();
        return switch (scope) {
            case "ROLE_MANAGER" -> getManagerByEmail(email);
            case "ROLE_STAFF" -> getStaffByEmail(email);
            case "ROLE_CUSTOMER" -> getCustomerByEmail(email);
            default -> throw new BadRequestException("Invalid user role");
        };
    }

    @Override
    public User updateCurrentUser(UserRequest request) {
        User user = getCurrentUser();
        user.setPhone(request.getPhone());
        user.setFullName(request.getFullName());
        user.setDob(request.getDob());
        user.setGender(request.getGender());
        return switch (user.getRole()) {
            case MANAGER -> managerRepository.save((Manager) user);
            case STAFF -> staffRepository.save((Staff) user);
            case CUSTOMER -> customerRepository.save((Customer) user);
        };
    }

    @Override
    public void deleteCurrentCustomer() {
        User user = getCurrentUser();
        if (Objects.requireNonNull(user.getRole()) == EUserRole.CUSTOMER) {
            customerRepository.delete((Customer) user);
        } else {
            throw new BadRequestException("Invalid user role");
        }
    }

    @Override
    public <T> UserResponse convertToUserResponse(T user) {
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public <T> Page<UserResponse> convertToUserResponse(Page<T> users) {
        return users.map(this::convertToUserResponse);
    }

}
