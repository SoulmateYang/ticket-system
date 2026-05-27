package com.scenic.ticket.controller;

import com.scenic.ticket.dto.OtaOrderResponse;
import com.scenic.ticket.dto.OtaOrderSyncRequest;
import com.scenic.ticket.dto.TicketResponse;
import com.scenic.ticket.model.OtaChannel;
import com.scenic.ticket.service.OtaOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OTA订单控制器
 * 提供OTA平台订单同步接口
 */
@RestController
@RequestMapping("/api/ota")
@RequiredArgsConstructor
public class OtaController {

    private final OtaOrderService otaOrderService;

    /**
     * 同步OTA订单
     */
    @PostMapping("/orders/sync")
    public ResponseEntity<Map<String, Object>> syncOrder(@Valid @RequestBody OtaOrderSyncRequest request) {
        OtaOrderResponse response = otaOrderService.syncOrder(request);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    /**
     * 获取待处理订单
     */
    @GetMapping("/orders/pending")
    public ResponseEntity<Map<String, Object>> getPendingOrders(
            @RequestParam(required = false) OtaChannel channel) {
        List<OtaOrderResponse> orders;
        if (channel != null) {
            orders = otaOrderService.getPendingOrdersByChannel(channel);
        } else {
            orders = otaOrderService.getAllPendingOrders();
        }
        return ResponseEntity.ok(Map.of("success", true, "data", orders));
    }

    /**
     * 根据订单生成票务
     */
    @PostMapping("/orders/{orderId}/generate")
    public ResponseEntity<Map<String, Object>> generateTickets(@PathVariable Long orderId) {
        List<TicketResponse> tickets = otaOrderService.generateTicketsFromOrder(orderId);
        return ResponseEntity.ok(Map.of("success", true, "data", tickets));
    }
}