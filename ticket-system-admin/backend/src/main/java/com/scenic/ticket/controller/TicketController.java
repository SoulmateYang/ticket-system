package com.scenic.ticket.controller;

import com.scenic.ticket.dto.SingleTicketCreateRequest;
import com.scenic.ticket.dto.TicketCreateRequest;
import com.scenic.ticket.dto.TicketResponse;
import com.scenic.ticket.dto.VerifyResult;
import com.scenic.ticket.service.AudioService;
import com.scenic.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 票务管理控制器
 * 提供年票/月票管理和验票核销接口
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final AudioService audioService;

    /**
     * 创建年票/月票
     */
    @PostMapping("/passes")
    public ResponseEntity<Map<String, Object>> createPass(@Valid @RequestBody TicketCreateRequest request) {
        TicketResponse response = ticketService.createPass(request);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    /**
     * 激活年票/月票
     */
    @PostMapping("/passes/{id}/activate")
    public ResponseEntity<Map<String, Object>> activatePass(@PathVariable Long id) {
        TicketResponse response = ticketService.activatePass(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    /**
     * 暂停年票/月票
     */
    @PostMapping("/passes/{id}/suspend")
    public ResponseEntity<Map<String, Object>> suspendPass(@PathVariable Long id) {
        TicketResponse response = ticketService.suspendPass(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    /**
     * 取消年票/月票
     */
    @PostMapping("/passes/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelPass(@PathVariable Long id) {
        TicketResponse response = ticketService.cancelPass(id);
        return ResponseEntity.ok(Map.of("success", true, "data", response));
    }

    /**
     * 查询访客的有效年票/月票
     */
    @GetMapping("/passes/visitor/{visitorId}")
    public ResponseEntity<Map<String, Object>> findValidPasses(@PathVariable String visitorId) {
        List<TicketResponse> passes = ticketService.findValidPasses(visitorId);
        return ResponseEntity.ok(Map.of("success", true, "data", passes));
    }

    /**
     * 分页查询所有年票/月票
     */
    @GetMapping("/passes")
    public ResponseEntity<Map<String, Object>> getAllPasses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TicketResponse> passes = ticketService.findAllPasses(PageRequest.of(page, size));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", passes.getContent(),
                "totalElements", passes.getTotalElements(),
                "totalPages", passes.getTotalPages()
        ));
    }

    /**
     * 批量创建次票
     */
    @PostMapping("/single")
    public ResponseEntity<Map<String, Object>> createSingleTickets(
            @Valid @RequestBody SingleTicketCreateRequest request) {
        List<TicketResponse> tickets = ticketService.createSingleTickets(request);
        return ResponseEntity.ok(Map.of("success", true, "data", tickets));
    }

    /**
     * 验票核销
     */
    @PostMapping("/verify")
    public ResponseEntity<VerifyResult> verifyTicket(
            @RequestBody Map<String, String> request) {
        String ticketCode = request.get("ticketCode");
        String verifiedBy = request.get("verifiedBy");
        String deviceId = request.get("deviceId");
        VerifyResult result = ticketService.verifyTicket(ticketCode, verifiedBy, deviceId);

        if (result.isSuccess()) {
            audioService.playSuccess();
        } else {
            audioService.playFail();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 分页查询所有票据（次票）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TicketResponse> tickets = ticketService.findAllTickets(PageRequest.of(page, size));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", tickets.getContent(),
                "totalElements", tickets.getTotalElements(),
                "totalPages", tickets.getTotalPages()
        ));
    }
}