package com.scenic.ticket.service;

import com.scenic.ticket.dto.SingleTicketCreateRequest;
import com.scenic.ticket.dto.TicketCreateRequest;
import com.scenic.ticket.dto.TicketResponse;
import com.scenic.ticket.dto.VerifyResult;
import com.scenic.ticket.exception.BusinessException;
import com.scenic.ticket.model.EntryLog;
import com.scenic.ticket.model.Ticket;
import com.scenic.ticket.model.TicketType;
import com.scenic.ticket.repository.EntryLogRepository;
import com.scenic.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 票务核心服务
 * 包含年票/月票管理和核销逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EntryLogRepository entryLogRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String VERIFY_LOCK_PREFIX = "ticket:lock:";
    private static final long LOCK_TIMEOUT_SECONDS = 5;

    /**
     * 创建年票/月票
     */
    @Transactional
    public TicketResponse createPass(TicketCreateRequest request) {
        if (request.getType() != TicketType.YEAR_PASS && request.getType() != TicketType.MONTH_PASS) {
            throw new BusinessException("只能创建年票或月票");
        }

        String ticketCode = UUID.randomUUID().toString();
        Ticket ticket = Ticket.builder()
                .type(request.getType())
                .ticketCode(ticketCode)
                .visitorId(request.getVisitorId())
                .visitorName(request.getVisitorName())
                .phone(request.getPhone())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .maxEntries(request.getMaxEntries())
                .usedEntries(0)
                .maxEntriesExceededAction(request.getMaxEntriesExceededAction() != null
                        ? request.getMaxEntriesExceededAction() : "REJECT")
                .status("AVAILABLE")
                .channel("WINDOW")
                .build();

        ticket = ticketRepository.save(ticket);
        log.info("创建{}成功: ticketCode={}, visitorId={}", request.getType(), ticketCode, request.getVisitorId());
        return toResponse(ticket);
    }

    /**
     * 激活年票/月票
     */
    @Transactional
    public TicketResponse activatePass(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("票不存在"));
        ticket.setStatus("AVAILABLE");
        ticket = ticketRepository.save(ticket);
        log.info("激活票成功: ticketId={}", ticketId);
        return toResponse(ticket);
    }

    /**
     * 暂停年票/月票
     */
    @Transactional
    public TicketResponse suspendPass(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("票不存在"));
        ticket.setStatus("SUSPENDED");
        ticket = ticketRepository.save(ticket);
        log.info("暂停票成功: ticketId={}", ticketId);
        return toResponse(ticket);
    }

    /**
     * 取消年票/月票
     */
    @Transactional
    public TicketResponse cancelPass(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException("票不存在"));
        ticket.setStatus("CANCELLED");
        ticket = ticketRepository.save(ticket);
        log.info("取消票成功: ticketId={}", ticketId);
        return toResponse(ticket);
    }

    /**
     * 核销验票
     * 使用Redis分布式锁防止重复验票
     */
    @Transactional
    public VerifyResult verifyTicket(String ticketCode, String verifiedBy, String deviceId) {
        String lockKey = VERIFY_LOCK_PREFIX + ticketCode;
        Boolean locked = false;

        try {
            locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                return VerifyResult.builder()
                        .success(false)
                        .code("LOCK_FAILED")
                        .message("验票繁忙，请稍后重试")
                        .build();
            }

            Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                    .orElseThrow(() -> new BusinessException("TICKET_NOT_FOUND", "票不存在"));

            if (!"AVAILABLE".equals(ticket.getStatus())) {
                logEntry(ticket.getId(), ticketCode, ticket.getVisitorId(), ticket.getType().name(), "FAIL",
                        "票状态异常: " + ticket.getStatus(), verifiedBy, deviceId);
                return VerifyResult.builder()
                        .success(false)
                        .code("INVALID_STATUS")
                        .message("票状态异常: " + ticket.getStatus())
                        .ticketCode(ticketCode)
                        .build();
            }

            LocalDateTime now = LocalDateTime.now();
            if (ticket.getValidFrom() != null && now.isBefore(ticket.getValidFrom())) {
                logEntry(ticket.getId(), ticketCode, ticket.getVisitorId(), ticket.getType().name(), "FAIL",
                        "票未生效", verifiedBy, deviceId);
                return VerifyResult.builder()
                        .success(false)
                        .code("NOT_YET_VALID")
                        .message("票未生效，有效期: " + ticket.getValidFrom())
                        .ticketCode(ticketCode)
                        .build();
            }

            if (ticket.getValidTo() != null && now.isAfter(ticket.getValidTo())) {
                logEntry(ticket.getId(), ticketCode, ticket.getVisitorId(), ticket.getType().name(), "FAIL",
                        "票已过期", verifiedBy, deviceId);
                return VerifyResult.builder()
                        .success(false)
                        .code("EXPIRED")
                        .message("票已过期")
                        .ticketCode(ticketCode)
                        .build();
            }

            if ((ticket.getType() == TicketType.YEAR_PASS || ticket.getType() == TicketType.MONTH_PASS)) {
                if (ticket.getMaxEntries() != null && ticket.getUsedEntries() >= ticket.getMaxEntries()) {
                    String action = ticket.getMaxEntriesExceededAction();
                    if ("REJECT".equals(action)) {
                        logEntry(ticket.getId(), ticketCode, ticket.getVisitorId(), ticket.getType().name(), "REJECT",
                                "入园次数已用完", verifiedBy, deviceId);
                        return VerifyResult.builder()
                                .success(false)
                                .code("MAX_ENTRIES_EXCEEDED")
                                .message("入园次数已用完")
                                .ticketCode(ticketCode)
                                .build();
                    }
                }
            }

            ticket.setStatus("USED");
            ticket.setUsedAt(now);
            ticket.setUsedEntries(ticket.getUsedEntries() + 1);
            ticketRepository.save(ticket);

            logEntry(ticket.getId(), ticketCode, ticket.getVisitorId(), ticket.getType().name(), "SUCCESS",
                    null, verifiedBy, deviceId);

            log.info("验票成功: ticketCode={}, visitor={}", ticketCode, ticket.getVisitorName());

            return VerifyResult.builder()
                    .success(true)
                    .code("SUCCESS")
                    .message("验票成功")
                    .ticketCode(ticketCode)
                    .ticketType(ticket.getType().name())
                    .visitorName(ticket.getVisitorName())
                    .entryType(ticket.getType().getDescription())
                    .build();

        } catch (BusinessException e) {
            log.error("验票业务异常: ticketCode={}, code={}", ticketCode, e.getCode(), e);
            return VerifyResult.builder()
                    .success(false)
                    .code(e.getCode())
                    .message(e.getMessage())
                    .ticketCode(ticketCode)
                    .build();
        } catch (Exception e) {
            log.error("验票异常: ticketCode={}", ticketCode, e);
            return VerifyResult.builder()
                    .success(false)
                    .code("SYSTEM_ERROR")
                    .message("系统异常，请稍后重试")
                    .build();
        } finally {
            if (Boolean.TRUE.equals(locked)) {
                redisTemplate.delete(lockKey);
            }
        }
    }

    /**
     * 查询有效年票/月票
     */
    public List<TicketResponse> findValidPasses(String visitorId) {
        List<TicketType> passTypes = List.of(TicketType.YEAR_PASS, TicketType.MONTH_PASS);
        List<Ticket> tickets = ticketRepository.findValidPasses(visitorId, passTypes, LocalDateTime.now());
        return tickets.stream().map(this::toResponse).toList();
    }

    /**
     * 分页查询所有年票/月票
     */
    public Page<TicketResponse> findAllPasses(Pageable pageable) {
        List<TicketType> passTypes = List.of(TicketType.YEAR_PASS, TicketType.MONTH_PASS);
        Page<Ticket> page = ticketRepository.findByTypeIn(passTypes, pageable);
        return page.map(this::toResponse);
    }

    /**
     * 分页查询所有票据（次票）
     */
    public Page<TicketResponse> findAllTickets(Pageable pageable) {
        Page<Ticket> page = ticketRepository.findAll(pageable);
        return page.map(this::toResponse);
    }

    /**
     * 批量创建次票
     */
    @Transactional
    public List<TicketResponse> createSingleTickets(SingleTicketCreateRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BusinessException("VALIDATION_ERROR", "数量必须大于0");
        }

        String channel = request.getChannel() != null ? request.getChannel() : "WINDOW";
        List<Ticket> tickets = new java.util.ArrayList<>();

        for (int i = 0; i < request.getQuantity(); i++) {
            String ticketCode = UUID.randomUUID().toString();
            Ticket ticket = Ticket.builder()
                    .type(TicketType.SINGLE_USE)
                    .ticketCode(ticketCode)
                    .visitorName(request.getPerformanceName())
                    .status("AVAILABLE")
                    .channel(channel)
                    .validFrom(LocalDateTime.now())
                    .validTo(LocalDateTime.now().plusDays(1))
                    .maxEntries(1)
                    .usedEntries(0)
                    .maxEntriesExceededAction("REJECT")
                    .build();
            tickets.add(ticket);
        }

        tickets = ticketRepository.saveAll(tickets);
        log.info("批量创建次票成功: performance={}, quantity={}", request.getPerformanceName(), request.getQuantity());
        return tickets.stream().map(this::toResponse).toList();
    }

    private void logEntry(Long ticketId, String ticketCode, String visitorId, String ticketType,
                          String result, String remark, String verifiedBy, String deviceId) {
        EntryLog entryLog = EntryLog.builder()
                .ticketId(ticketId)
                .ticketCode(ticketCode)
                .visitorId(visitorId)
                .entryType(ticketType)
                .channel("WINDOW")
                .entryTime(LocalDateTime.now())
                .result(result)
                .remark(remark)
                .verifiedBy(verifiedBy)
                .deviceId(deviceId)
                .build();
        entryLogRepository.save(entryLog);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .type(ticket.getType())
                .ticketCode(ticket.getTicketCode())
                .visitorId(ticket.getVisitorId())
                .visitorName(ticket.getVisitorName())
                .phone(ticket.getPhone())
                .validFrom(ticket.getValidFrom())
                .validTo(ticket.getValidTo())
                .maxEntries(ticket.getMaxEntries())
                .usedEntries(ticket.getUsedEntries())
                .maxEntriesExceededAction(ticket.getMaxEntriesExceededAction())
                .status(ticket.getStatus())
                .channel(ticket.getChannel())
                .usedAt(ticket.getUsedAt())
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}