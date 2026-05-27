package com.scenic.ticket.repository;

import com.scenic.ticket.model.EntryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntryLogRepository extends JpaRepository<EntryLog, Long> {

    List<EntryLog> findByTicketId(Long ticketId);

    List<EntryLog> findByTicketCode(String ticketCode);

    List<EntryLog> findByVisitorId(String visitorId);

    @Query("SELECT e FROM EntryLog e WHERE e.channel = :channel AND e.entryTime BETWEEN :start AND :end")
    List<EntryLog> findByChannelAndTimeRange(@Param("channel") String channel,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @Query("SELECT e FROM EntryLog e WHERE e.entryTime BETWEEN :start AND :end ORDER BY e.entryTime DESC")
    List<EntryLog> findByTimeRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(e) FROM EntryLog e WHERE e.channel = :channel AND e.entryTime >= :since")
    long countByChannelSince(@Param("channel") String channel, @Param("since") LocalDateTime since);
}