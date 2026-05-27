package com.scenic.ticket.service;

import com.scenic.ticket.dto.StaffCreateRequest;
import com.scenic.ticket.dto.StaffResponse;
import com.scenic.ticket.model.Staff;
import com.scenic.ticket.model.StaffRole;
import com.scenic.ticket.model.StaffStatus;
import com.scenic.ticket.repository.StaffRepository;
import com.scenic.ticket.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public Page<StaffResponse> findAll(Pageable pageable) {
        return staffRepository.findAll(pageable).map(this::toResponse);
    }

    public StaffResponse findById(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new BusinessException("STAFF_NOT_FOUND", "员工不存在"));
        return toResponse(staff);
    }

    @Transactional
    public StaffResponse create(StaffCreateRequest request) {
        if (staffRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("DUPLICATE_PHONE", "手机号已注册");
        }
        if (staffRepository.existsByEmployeeNo(request.getEmployeeNo())) {
            throw new BusinessException("DUPLICATE_EMPLOYEE_NO", "工号已存在");
        }

        StaffRole role;
        try {
            role = StaffRole.valueOf(request.getRole());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("INVALID_ROLE", "无效的角色: " + request.getRole());
        }

        Staff staff = Staff.builder()
                .employeeNo(request.getEmployeeNo())
                .name(request.getName())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .status(StaffStatus.ACTIVE)
                .build();

        staff = staffRepository.save(staff);
        return toResponse(staff);
    }

    @Transactional
    public void delete(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new BusinessException("STAFF_NOT_FOUND", "员工不存在");
        }
        staffRepository.deleteById(id);
    }

    @Transactional
    public StaffResponse updateRole(Long id, String role) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new BusinessException("STAFF_NOT_FOUND", "员工不存在"));

        StaffRole newRole;
        try {
            newRole = StaffRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("INVALID_ROLE", "无效的角色: " + role);
        }

        staff.setRole(newRole);
        staff = staffRepository.save(staff);
        return toResponse(staff);
    }

    @Transactional
    public StaffResponse suspend(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new BusinessException("STAFF_NOT_FOUND", "员工不存在"));
        staff.setStatus(StaffStatus.SUSPENDED);
        staff = staffRepository.save(staff);
        return toResponse(staff);
    }

    @Transactional
    public StaffResponse activate(Long id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new BusinessException("STAFF_NOT_FOUND", "员工不存在"));
        staff.setStatus(StaffStatus.ACTIVE);
        staff = staffRepository.save(staff);
        return toResponse(staff);
    }

    private StaffResponse toResponse(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .employeeNo(staff.getEmployeeNo())
                .name(staff.getName())
                .phone(staff.getPhone())
                .role(staff.getRole().name())
                .status(staff.getStatus().name())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}