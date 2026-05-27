package com.scenic.ticket.repository;

import com.scenic.ticket.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByPhone(String phone);

    Optional<Staff> findByEmployeeNo(String employeeNo);

    boolean existsByPhone(String phone);

    boolean existsByEmployeeNo(String employeeNo);
}