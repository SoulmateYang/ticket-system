package com.scenic.ticket.repository;

import com.scenic.ticket.model.EntryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EntryRecordRepository extends JpaRepository<EntryRecord, Long> {
    List<EntryRecord> findByTicketId(Long ticketId);
    List<EntryRecord> findByVisitorId(String visitorId);
}