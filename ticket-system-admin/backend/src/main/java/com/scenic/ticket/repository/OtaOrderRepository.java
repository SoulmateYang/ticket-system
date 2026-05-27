package com.scenic.ticket.repository;

import com.scenic.ticket.model.OtaChannel;
import com.scenic.ticket.model.OtaOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtaOrderRepository extends JpaRepository<OtaOrder, Long> {

    Optional<OtaOrder> findByExternalOrderId(String externalOrderId);

    List<OtaOrder> findByOtaChannelAndSyncStatus(OtaChannel channel, String syncStatus);

    List<OtaOrder> findBySyncStatus(String syncStatus);

    @Query("SELECT o FROM OtaOrder o WHERE o.syncStatus = 'PENDING' ORDER BY o.createdAt ASC")
    List<OtaOrder> findPendingOrders();

    @Modifying
    @Query("UPDATE OtaOrder o SET o.syncStatus = :status, o.syncTime = :syncTime WHERE o.id = :id")
    int updateSyncStatus(@Param("id") Long id, @Param("status") String status, @Param("syncTime") LocalDateTime syncTime);
}