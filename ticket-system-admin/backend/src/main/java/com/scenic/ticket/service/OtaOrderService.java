package com.scenic.ticket.service;

import com.scenic.ticket.dto.OtaOrderResponse;
import com.scenic.ticket.dto.OtaOrderSyncRequest;
import com.scenic.ticket.dto.SingleTicketCreateRequest;
import com.scenic.ticket.dto.TicketResponse;
import com.scenic.ticket.exception.BusinessException;
import com.scenic.ticket.model.OtaChannel;
import com.scenic.ticket.model.OtaOrder;
import com.scenic.ticket.model.TicketType;
import com.scenic.ticket.repository.OtaOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OTA订单服务
 * 处理OTA平台订单同步和票务生成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtaOrderService {

    private final OtaOrderRepository otaOrderRepository;
    private final TicketService ticketService;

    /**
     * 同步OTA订单
     */
    @Transactional
    public OtaOrderResponse syncOrder(OtaOrderSyncRequest request) {
        if (otaOrderRepository.findByExternalOrderId(request.getExternalOrderId()).isPresent()) {
            throw new BusinessException("DUPLICATE_ORDER", "订单已存在: " + request.getExternalOrderId());
        }

        OtaOrder order = OtaOrder.builder()
                .otaChannel(request.getChannel())
                .externalOrderId(request.getExternalOrderId())
                .ticketType(TicketType.OTA_TICKET)
                .performanceName(request.getPerformanceName())
                .quantity(request.getQuantity())
                .amount(request.getAmount())
                .buyerName(request.getBuyerName())
                .buyerPhone(request.getBuyerPhone())
                .buyerIdCard(request.getBuyerIdCard())
                .status("PENDING")
                .syncStatus("SYNCED")
                .syncTime(LocalDateTime.now())
                .build();

        order = otaOrderRepository.save(order);
        log.info("OTA订单同步成功: channel={}, externalOrderId={}",
                request.getChannel(), request.getExternalOrderId());

        return toResponse(order);
    }

    /**
     * 根据OTA渠道获取待处理订单
     */
    public List<OtaOrderResponse> getPendingOrdersByChannel(OtaChannel channel) {
        return otaOrderRepository.findByOtaChannelAndSyncStatus(channel, "PENDING")
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有待处理订单
     */
    public List<OtaOrderResponse> getAllPendingOrders() {
        return otaOrderRepository.findPendingOrders()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 生成OTA票务（根据同步的订单生成对应次票）
     */
    @Transactional
    public List<TicketResponse> generateTicketsFromOrder(Long orderId) {
        OtaOrder order = otaOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("订单不存在"));

        if (!"SYNCED".equals(order.getSyncStatus())) {
            throw new BusinessException("订单未同步或已处理");
        }

        SingleTicketCreateRequest ticketRequest = SingleTicketCreateRequest.builder()
                .quantity(order.getQuantity())
                .performanceName(order.getPerformanceName())
                .channel(order.getOtaChannel().name())
                .build();

        List<TicketResponse> tickets = ticketService.createSingleTickets(ticketRequest);

        order.setStatus("TICKETS_GENERATED");
        order.setSyncStatus("COMPLETED");
        order.setSyncTime(LocalDateTime.now());
        otaOrderRepository.save(order);

        log.info("OTA订单生成票务成功: orderId={}, ticketCount={}", orderId, tickets.size());
        return tickets;
    }

    private OtaOrderResponse toResponse(OtaOrder order) {
        return OtaOrderResponse.builder()
                .id(order.getId())
                .otaChannel(order.getOtaChannel())
                .externalOrderId(order.getExternalOrderId())
                .performanceName(order.getPerformanceName())
                .quantity(order.getQuantity())
                .amount(order.getAmount())
                .buyerName(order.getBuyerName())
                .buyerPhone(order.getBuyerPhone())
                .buyerIdCard(order.getBuyerIdCard())
                .status(order.getStatus())
                .syncStatus(order.getSyncStatus())
                .syncTime(order.getSyncTime())
                .createdAt(order.getCreatedAt())
                .build();
    }
}