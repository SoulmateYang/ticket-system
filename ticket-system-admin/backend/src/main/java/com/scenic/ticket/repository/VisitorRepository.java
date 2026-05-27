package com.scenic.ticket.repository;

import com.scenic.ticket.model.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    Optional<Visitor> findByIdCard(String idCard);
    Optional<Visitor> findByPhone(String phone);
    boolean existsByIdCard(String idCard);
}