package com.scenic.ticket.repository;

import com.scenic.ticket.model.Ticket;
import com.scenic.ticket.model.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.ticketCode = :ticketCode")
    Optional<Ticket> findByTicketCodeWithLock(@Param("ticketCode") String ticketCode);

    List<Ticket> findByVisitorId(String visitorId);

    List<Ticket> findByTypeAndStatus(TicketType type, String status);

    @Query("SELECT t FROM Ticket t WHERE t.type IN :types AND t.status = :status")
    List<Ticket> findByTypesAndStatus(@Param("types") List<TicketType> types, @Param("status") String status);

    @Query("SELECT t FROM Ticket t WHERE t.visitorId = :visitorId AND t.type IN :types AND t.status = 'AVAILABLE' " +
           "AND t.validFrom <= :now AND t.validTo >= :now")
    List<Ticket> findValidPasses(@Param("visitorId") String visitorId,
                                 @Param("types") List<TicketType> types,
                                 @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.type = :type AND t.status = 'USED' AND t.usedAt >= :since")
    long countUsedSince(@Param("type") TicketType type, @Param("since") LocalDateTime since);

    List<Ticket> findByWindowSaleId(Long windowSaleId);

    Page<Ticket> findByTypeIn(List<TicketType> types, Pageable pageable);
}