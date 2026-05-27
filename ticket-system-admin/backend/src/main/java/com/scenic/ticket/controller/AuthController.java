package com.scenic.ticket.controller;

import com.scenic.ticket.dto.LoginRequest;
import com.scenic.ticket.dto.LoginResponse;
import com.scenic.ticket.dto.PasswordChangeRequest;
import com.scenic.ticket.exception.BusinessException;
import com.scenic.ticket.model.Staff;
import com.scenic.ticket.model.StaffStatus;
import com.scenic.ticket.repository.StaffRepository;
import com.scenic.ticket.security.LoginRateLimiter;
import com.scenic.ticket.util.JwtTokenUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final LoginRateLimiter rateLimiter;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String phone = request.getPhone();

        if (rateLimiter.isLocked(phone)) {
            throw new BusinessException("ACCOUNT_LOCKED", "登录失败次数过多，请15分钟后再试");
        }

        try {
            Staff staff = staffRepository.findByPhone(phone)
                    .orElseThrow(() -> new BusinessException("AUTH_FAILED", "手机号或密码错误"));

            if (!passwordEncoder.matches(request.getPassword(), staff.getPasswordHash())) {
                rateLimiter.recordFailure(phone);
                throw new BusinessException("AUTH_FAILED", "手机号或密码错误");
            }

            if (staff.getStatus() == StaffStatus.SUSPENDED) {
                throw new BusinessException("ACCOUNT_SUSPENDED", "账号已停用");
            }

            rateLimiter.recordSuccess(phone);

            String token = jwtTokenUtil.generateToken(
                    staff.getId(),
                    staff.getEmployeeNo(),
                    staff.getName(),
                    staff.getRole().name()
            );

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .staff(LoginResponse.StaffDto.builder()
                            .id(staff.getId())
                            .employeeNo(staff.getEmployeeNo())
                            .name(staff.getName())
                            .role(staff.getRole().name())
                            .build())
                    .build();

            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            if (!"ACCOUNT_LOCKED".equals(e.getCode()) && !"ACCOUNT_SUSPENDED".equals(e.getCode())) {
                rateLimiter.recordFailure(phone);
            }
            throw e;
        }
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            Authentication authentication) {

        Long staffId = (Long) authentication.getPrincipal();
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new BusinessException("STAFF_NOT_FOUND", "员工不存在"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), staff.getPasswordHash())) {
            throw new BusinessException("INVALID_PASSWORD", "当前密码错误");
        }

        staff.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        staffRepository.save(staff);

        return ResponseEntity.ok().build();
    }
}