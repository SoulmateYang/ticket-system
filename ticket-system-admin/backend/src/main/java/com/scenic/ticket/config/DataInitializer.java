package com.scenic.ticket.config;

import com.scenic.ticket.model.Staff;
import com.scenic.ticket.model.StaffRole;
import com.scenic.ticket.model.StaffStatus;
import com.scenic.ticket.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!staffRepository.existsByEmployeeNo("ADMIN001")) {
            Staff admin = Staff.builder()
                    .employeeNo("ADMIN001")
                    .name("系统管理员")
                    .phone("13800000000")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(StaffRole.ADMIN)
                    .status(StaffStatus.ACTIVE)
                    .build();
            staffRepository.save(admin);
            log.info("Created default admin account: ADMIN001 / admin123");
        }
    }
}