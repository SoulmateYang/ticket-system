package com.scenic.ticket.repository;

import com.scenic.ticket.model.WindowSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WindowSaleRepository extends JpaRepository<WindowSale, Long> {
}