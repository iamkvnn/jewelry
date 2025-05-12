package com.web.jewelry.service.user;

import com.web.jewelry.dto.request.EmailRequest;
import com.web.jewelry.dto.request.ResetPasswordRequest;
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
import com.web.jewelry.service.email.EmailQueueService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService implements IUserService {
    private final StaffRepository staffRepository;
    private final ModelMapper modelMapper;
    private final CustomerRepository customerRepository;
    private final ManagerRepository managerRepository;
    private final EmailQueueService emailQueueService;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, String> verificationCodes = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

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
    public Set<Long> getAllUerRegisteredForNews() {
        return customerRepository.findAllByIsSubscribedForNews().stream()
                .filter(customer -> customer.getStatus() == EUserStatus.ACTIVE)
                .map(Customer::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public Page<Staff> findStaffByName(String name, Pageable pageable) {
        return staffRepository.findByFullNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<Customer> findCustomerByName(String name, Pageable pageable) {
        return customerRepository.findByFullNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            String token = UUID.randomUUID().toString();
            user.setBackupToken(token);
            user.setBackupTokenExpireAt(LocalDateTime.now().plusDays(1));
            switch (user.getRole()) {
                case MANAGER -> managerRepository.save((Manager) user);
                case STAFF -> staffRepository.save((Staff) user);
                case CUSTOMER -> customerRepository.save((Customer) user);
            }
            emailQueueService.enqueue(new EmailRequest(user.getEmail(), "Xác nhận thay đổi mật khẩu", "Mật khẩu của bạn đã được thay đổi thành công.\n" +
                    "Nếu bạn không thực hiện thay đổi này, vui lòng nhấp vào liên kết sau để khôi phục mật khẩu của bạn: https://shinyjewelry.shop/recover-password?token=" + token));
        } else {
            throw new BadRequestException("Old password is incorrect");
        }
    }

    @Override
    public void sendEmailResetPassword(String email, EUserRole role) {
        User user = switch (role) {
            case MANAGER -> getManagerByEmail(email);
            case STAFF -> getStaffByEmail(email);
            case CUSTOMER -> getCustomerByEmail(email);
        };
        String code = generateVerificationCode();
        String roleString = switch (role) {
            case MANAGER -> "quản lý";
            case STAFF -> "nhân viên";
            case CUSTOMER -> "thành viên";
        };
        emailQueueService.enqueue(new EmailRequest(email, "Xác nhận khôi phục mật khẩu tài khoản " + roleString + " tại Shiny", "Mã xác nhận của bạn là: " + code));
        verificationCodes.put(user.getEmail() + user.getRole(), code);
        scheduleCodeExpiration(user.getEmail() + user.getRole(), code);
    }

    private void scheduleCodeExpiration(String key, String code) {
        scheduler.schedule(() -> {
            if (verificationCodes.containsKey(key) && verificationCodes.get(key).equals(code)) {
                verificationCodes.remove(key);
            }
        }, 120, TimeUnit.SECONDS);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (request.getToken() == null && request.getCode() == null) {
            throw new BadRequestException("Verification code is required");
        }
        User user;
        if (request.getToken() != null) {
            user = customerRepository.findByBackupToken(request.getToken()).orElse(null);
            if (user == null) {
                user = staffRepository.findByBackupToken(request.getToken()).orElse(null);
            }
            if (user == null) {
                user = managerRepository.findByBackupToken(request.getToken()).orElse(null);
            }
            if (user == null || user.getBackupTokenExpireAt().isBefore(LocalDateTime.now()) || !user.getBackupToken().equals(request.getToken())) {
                throw new BadRequestException("Invalid token");
            }
        } else {
            if (verificationCodes.containsKey(request.getEmail() + request.getRole()) && verificationCodes.get(request.getEmail() + request.getRole()).equals(request.getCode())) {
                verificationCodes.remove(request.getEmail() + request.getRole());
            } else {
                throw new BadRequestException("Invalid verification code");
            }
            user = switch (request.getRole()) {
                case MANAGER -> getManagerByEmail(request.getEmail());
                case STAFF -> getStaffByEmail(request.getEmail());
                case CUSTOMER -> getCustomerByEmail(request.getEmail());
            };
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setBackupToken(null);
        user.setBackupTokenExpireAt(null);
        switch (user.getRole()) {
            case MANAGER -> managerRepository.save((Manager) user);
            case STAFF -> staffRepository.save((Staff) user);
            case CUSTOMER -> customerRepository.save((Customer) user);
        }
    }

    @Override
    public void setRegisterForNews(boolean isSubscribed) {
        User user = getCurrentUser();
        if (user instanceof Customer customer) {
            customer.setIsSubscribedForNews(isSubscribed);
            customerRepository.save(customer);
        } else {
            throw new BadRequestException("Invalid user role");
        }
    }

    @Override
    public User createStaff(UserRequest request) {
        if (staffRepository.existsByEmail(request.getEmail())) {
            throw new AlreadyExistException("Email already exists");
        }
        if (staffRepository.existsByPhone(request.getPhone())) {
            throw new AlreadyExistException("Phone already exists");
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
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new AlreadyExistException("Phone already exists");
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
        User user = switch (scope) {
            case "ROLE_MANAGER" -> getManagerByEmail(email);
            case "ROLE_STAFF" -> getStaffByEmail(email);
            case "ROLE_CUSTOMER" -> getCustomerByEmail(email);
            default -> throw new BadRequestException("Invalid user role");
        };
        if (user.getStatus() == EUserStatus.REMOVED) {
            throw new ResourceNotFoundException("User not found");
        }
        if (user.getStatus() == EUserStatus.BANNED) {
            throw new BadRequestException("Account has been banned");
        }
        return user;
    }

    @Override
    public User updateCurrentUser(UserRequest request) {
        User user = getCurrentUser();
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
    public void sendRequestDeleteCurrentCustomer() {
        Customer user = (Customer) getCurrentUser();
        if (Objects.requireNonNull(user.getRole()) == EUserRole.CUSTOMER) {
            String token = UUID.randomUUID().toString();
            user.setDeleteAccountToken(token);
            user.setDeleteAccountTokenExpiration(LocalDateTime.now().plusMinutes(5));
            emailQueueService.enqueue(new EmailRequest(user.getEmail(), "Xác nhận xóa tài khoản", "Tài khoản của bạn đã được gửi yêu cầu xóa\n" +
                "Nếu bạn không thực hiện thay đổi này vui lòng bỏ qua email này. Nếu thực sự muốn xoóa, vui lòng nhấp vào liên kết sau trong vòng 5 phút để xóa tài khoản của bạn: https://shinyjewelry.shop/confirm-delete?token=" + token));
            customerRepository.save(user);
        } else {
            throw new BadRequestException("Invalid user role");
        }
    }

    @Override
    public void confirmDeleteCurrentCustomer(String token) {
        Customer user = customerRepository.findByDeleteAccountToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid token"));
        if (user.getDeleteAccountToken() != null && user.getDeleteAccountTokenExpiration() != null) {
            if (user.getDeleteAccountTokenExpiration().isBefore(LocalDateTime.now()) || !user.getDeleteAccountToken().equals(token)) {
                throw new BadRequestException("Invalid token");
            }
            user.setStatus(EUserStatus.REMOVED);
            customerRepository.save(user);
        } else {
            throw new BadRequestException("Invalid token");
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

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }
}
