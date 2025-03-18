package com.web.jewelry.service.user;

import com.web.jewelry.dto.request.UserRequest;
import com.web.jewelry.dto.response.UserResponse;
import com.web.jewelry.enums.EMembershiprank;
import com.web.jewelry.enums.EUserRole;
import com.web.jewelry.enums.EUserStatus;
import com.web.jewelry.exception.AlreadyExistException;
import com.web.jewelry.exception.ResourceNotFoundException;
import com.web.jewelry.model.Customer;
import com.web.jewelry.model.Manager;
import com.web.jewelry.model.Staff;
import com.web.jewelry.model.User;
import com.web.jewelry.repository.CustomerRepository;
import com.web.jewelry.repository.ManagerRepository;
import com.web.jewelry.repository.StaffRepository;
import com.web.jewelry.service.cart.ICartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService {
    private final StaffRepository staffRepository;
    private final ModelMapper modelMapper;
    private final CustomerRepository customerRepository;
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ICartService cartService;

    @PreAuthorize("hasRole('MANAGER')")
    @Override
    public Page<Staff> getAllStaff(Pageable pageable) {
        return staffRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Override
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }
    @PreAuthorize("hasRole('MANAGER')")
    @Override
    public Page<Manager> getAllManagers(Pageable pageable) {
        return null;
    }

    @PreAuthorize("hasRole('MANAGER')")
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
        Customer user = customerRepository.save(Customer.builder()
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
        cartService.initializeNewCart(user);
        return user;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'STAFF')")
    @PostAuthorize("returnObject.username == authentication.name")
    @Override
    public User getStaffById(Long id) {
        return staffRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'CUSTOMER')")
    @PostAuthorize("returnObject.username == authentication.name")
    @Override
    public User getCustomerById(Long id) {
        return customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostAuthorize("returnObject.username == authentication.name")
    @Override
    public User getManagerById(Long id) {
        return managerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
    }

    @Override
    public User getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Override
    public User getManagerByUsername(String username) {
        return managerRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
    }

    @Override
    public User getStaffByUsername(String username) {
        return staffRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
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
    public User updateCustomer(UserRequest request, Long id) {
        return customerRepository.findById(id)
                .map(customer -> {
                    customer.setPhone(request.getPhone());
                    customer.setFullName(request.getFullName());
                    customer.setDob(request.getDob());
                    customer.setGender(request.getGender());
                    return customerRepository.save(customer);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Override
    public User updateManager(UserRequest request, Long id) {
        return managerRepository.findById(id)
                .map(manager -> {
                    manager.setPhone(request.getPhone());
                    manager.setFullName(request.getFullName());
                    manager.setDob(request.getDob());
                    manager.setGender(request.getGender());
                    return managerRepository.save(manager);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
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
        return Optional.ofNullable(customerRepository.findByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public User updateCurrentUser(UserRequest request) {
        return null;
    }

    @Override
    public User deleteCurrentUser() {
        return null;
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
