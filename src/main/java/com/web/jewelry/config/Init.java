package com.web.jewelry.config;

import com.web.jewelry.enums.EUserRole;
import com.web.jewelry.enums.EUserStatus;
import com.web.jewelry.model.Manager;
import com.web.jewelry.repository.ManagerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Transactional
@Component
public class Init implements ApplicationListener<ApplicationReadyEvent> {
    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        createManagerIfNotExist();
    }

    private void createManagerIfNotExist() {
        if (managerRepository.count() == 0) {
            managerRepository.save(Manager.builder()
                    .username("manager")
                    .email("23110119@student.hcmute.edu.vn")
                    .password(passwordEncoder.encode("123456"))
                    .fullName("Manager")
                    .role(EUserRole.MANAGER)
                    .status(EUserStatus.ACTIVE)
                    .joinAt(LocalDateTime.now())
                    .build());
        }
    }
}
