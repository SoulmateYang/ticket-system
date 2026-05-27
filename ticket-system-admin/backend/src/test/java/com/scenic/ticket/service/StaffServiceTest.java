package com.scenic.ticket.service;

import com.scenic.ticket.dto.StaffCreateRequest;
import com.scenic.ticket.dto.StaffResponse;
import com.scenic.ticket.exception.BusinessException;
import com.scenic.ticket.model.Staff;
import com.scenic.ticket.model.StaffRole;
import com.scenic.ticket.model.StaffStatus;
import com.scenic.ticket.repository.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StaffService staffService;

    @Test
    void create_Success() {
        StaffCreateRequest request = StaffCreateRequest.builder()
                .employeeNo("EMP001")
                .name("张三")
                .phone("13800138000")
                .password("password123")
                .role("ADMIN")
                .build();

        when(staffRepository.existsByPhone("13800138000")).thenReturn(false);
        when(staffRepository.existsByEmployeeNo("EMP001")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(staffRepository.save(any(Staff.class))).thenAnswer(invocation -> {
            Staff staff = invocation.getArgument(0);
            staff.setId(1L);
            staff.setCreatedAt(LocalDateTime.now());
            return staff;
        });

        StaffResponse response = staffService.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("EMP001", response.getEmployeeNo());
        assertEquals("张三", response.getName());
        assertEquals("13800138000", response.getPhone());
        assertEquals("ADMIN", response.getRole());
        assertEquals("ACTIVE", response.getStatus());
        verify(staffRepository).save(any(Staff.class));
    }

    @Test
    void create_ThrowsException_WhenDuplicatePhone() {
        StaffCreateRequest request = StaffCreateRequest.builder()
                .employeeNo("EMP001")
                .name("张三")
                .phone("13800138000")
                .password("password123")
                .role("ADMIN")
                .build();

        when(staffRepository.existsByPhone("13800138000")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> staffService.create(request));
        assertEquals("DUPLICATE_PHONE", exception.getCode());
        assertEquals("手机号已注册", exception.getMessage());
    }

    @Test
    void create_ThrowsException_WhenDuplicateEmployeeNo() {
        StaffCreateRequest request = StaffCreateRequest.builder()
                .employeeNo("EMP001")
                .name("张三")
                .phone("13800138000")
                .password("password123")
                .role("ADMIN")
                .build();

        when(staffRepository.existsByPhone("13800138000")).thenReturn(false);
        when(staffRepository.existsByEmployeeNo("EMP001")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> staffService.create(request));
        assertEquals("DUPLICATE_EMPLOYEE_NO", exception.getCode());
        assertEquals("工号已存在", exception.getMessage());
    }

    @Test
    void create_ThrowsException_WhenInvalidRole() {
        StaffCreateRequest request = StaffCreateRequest.builder()
                .employeeNo("EMP001")
                .name("张三")
                .phone("13800138000")
                .password("password123")
                .role("INVALID_ROLE")
                .build();

        when(staffRepository.existsByPhone("13800138000")).thenReturn(false);
        when(staffRepository.existsByEmployeeNo("EMP001")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> staffService.create(request));
        assertEquals("INVALID_ROLE", exception.getCode());
    }

    @Test
    void findById_Success() {
        Staff staff = Staff.builder()
                .id(1L)
                .employeeNo("EMP001")
                .name("张三")
                .phone("13800138000")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));

        StaffResponse response = staffService.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("EMP001", response.getEmployeeNo());
    }

    @Test
    void findById_ThrowsException_WhenNotFound() {
        when(staffRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> staffService.findById(999L));
        assertEquals("STAFF_NOT_FOUND", exception.getCode());
    }

    @Test
    void delete_Success() {
        when(staffRepository.existsById(1L)).thenReturn(true);
        doNothing().when(staffRepository).deleteById(1L);

        assertDoesNotThrow(() -> staffService.delete(1L));
        verify(staffRepository).deleteById(1L);
    }

    @Test
    void delete_ThrowsException_WhenNotFound() {
        when(staffRepository.existsById(999L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> staffService.delete(999L));
        assertEquals("STAFF_NOT_FOUND", exception.getCode());
    }

    @Test
    void updateRole_Success() {
        Staff staff = Staff.builder()
                .id(1L)
                .employeeNo("EMP001")
                .name("张三")
                .phone("13800138000")
                .role(StaffRole.TICKETER)
                .status(StaffStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);

        StaffResponse response = staffService.updateRole(1L, "ADMIN");

        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void updateRole_ThrowsException_WhenInvalidRole() {
        Staff staff = Staff.builder()
                .id(1L)
                .employeeNo("EMP001")
                .name("张三")
                .phone("13800138000")
                .role(StaffRole.TICKETER)
                .status(StaffStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));

        BusinessException exception = assertThrows(BusinessException.class, () -> staffService.updateRole(1L, "INVALID_ROLE"));
        assertEquals("INVALID_ROLE", exception.getCode());
    }

    @Test
    void suspend_Success() {
        Staff staff = Staff.builder()
                .id(1L)
                .employeeNo("EMP001")
                .name("张三")
                .phone("13800138000")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);

        StaffResponse response = staffService.suspend(1L);

        assertEquals("SUSPENDED", response.getStatus());
    }

    @Test
    void activate_Success() {
        Staff staff = Staff.builder()
                .id(1L)
                .employeeNo("EMP001")
                .name("张三")
                .phone("13800138000")
                .role(StaffRole.ADMIN)
                .status(StaffStatus.SUSPENDED)
                .createdAt(LocalDateTime.now())
                .build();

        when(staffRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(staffRepository.save(any(Staff.class))).thenReturn(staff);

        StaffResponse response = staffService.activate(1L);

        assertEquals("ACTIVE", response.getStatus());
    }
}
