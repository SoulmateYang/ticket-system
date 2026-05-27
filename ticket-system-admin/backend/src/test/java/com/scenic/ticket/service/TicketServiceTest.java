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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EntryLogRepository entryLogRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void createPass_Success_YearPass() {
        TicketCreateRequest request = TicketCreateRequest.builder()
                .type(TicketType.YEAR_PASS)
                .visitorName("张三")
                .visitorId("110101199001011234")
                .phone("13800138000")
                .validFrom(LocalDateTime.now())
                .validTo(LocalDateTime.now().plusYears(1))
                .maxEntries(100)
                .build();

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket ticket = invocation.getArgument(0);
            ticket.setId(1L);
            return ticket;
        });

        TicketResponse response = ticketService.createPass(request);

        assertNotNull(response);
        assertEquals(TicketType.YEAR_PASS, response.getType());
        assertEquals("张三", response.getVisitorName());
        assertEquals("AVAILABLE", response.getStatus());
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void createPass_ThrowsException_WhenTypeNotPass() {
        TicketCreateRequest request = TicketCreateRequest.builder()
                .type(TicketType.SINGLE_USE)
                .visitorName("张三")
                .visitorId("110101199001011234")
                .build();

        assertThrows(BusinessException.class, () -> ticketService.createPass(request));
    }

    @Test
    void activatePass_Success() {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .type(TicketType.YEAR_PASS)
                .status("SUSPENDED")
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponse response = ticketService.activatePass(1L);

        assertEquals("AVAILABLE", response.getStatus());
    }

    @Test
    void suspendPass_Success() {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .type(TicketType.YEAR_PASS)
                .status("AVAILABLE")
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponse response = ticketService.suspendPass(1L);

        assertEquals("SUSPENDED", response.getStatus());
    }

    @Test
    void cancelPass_Success() {
        Ticket ticket = Ticket.builder()
                .id(1L)
                .type(TicketType.YEAR_PASS)
                .status("AVAILABLE")
                .build();

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponse response = ticketService.cancelPass(1L);

        assertEquals("CANCELLED", response.getStatus());
    }

    @Test
    void verifyTicket_Success() {
        String ticketCode = "test-ticket-code";
        Ticket ticket = Ticket.builder()
                .id(1L)
                .type(TicketType.YEAR_PASS)
                .ticketCode(ticketCode)
                .visitorId("110101199001011234")
                .visitorName("张三")
                .status("AVAILABLE")
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(1))
                .maxEntries(100)
                .usedEntries(0)
                .build();

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(ticketRepository.findByTicketCode(ticketCode)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);
        when(entryLogRepository.save(any(EntryLog.class))).thenReturn(null);

        VerifyResult result = ticketService.verifyTicket(ticketCode, "staff001", "device001");

        assertTrue(result.isSuccess());
        assertEquals("SUCCESS", result.getCode());
        assertEquals("张三", result.getVisitorName());
    }

    @Test
    void verifyTicket_Fail_TicketNotFound() {
        String ticketCode = "non-existent-code";

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(ticketRepository.findByTicketCode(ticketCode)).thenReturn(Optional.empty());

        VerifyResult result = ticketService.verifyTicket(ticketCode, "staff001", "device001");

        assertFalse(result.isSuccess());
        assertEquals("TICKET_NOT_FOUND", result.getCode());
    }

    @Test
    void verifyTicket_Fail_TicketAlreadyUsed() {
        String ticketCode = "used-ticket-code";
        Ticket ticket = Ticket.builder()
                .id(1L)
                .type(TicketType.YEAR_PASS)
                .ticketCode(ticketCode)
                .status("USED")
                .build();

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(ticketRepository.findByTicketCode(ticketCode)).thenReturn(Optional.of(ticket));
        when(entryLogRepository.save(any(EntryLog.class))).thenReturn(null);

        VerifyResult result = ticketService.verifyTicket(ticketCode, "staff001", "device001");

        assertFalse(result.isSuccess());
        assertEquals("INVALID_STATUS", result.getCode());
    }

    @Test
    void verifyTicket_Fail_TicketExpired() {
        String ticketCode = "expired-ticket-code";
        Ticket ticket = Ticket.builder()
                .id(1L)
                .type(TicketType.YEAR_PASS)
                .ticketCode(ticketCode)
                .status("AVAILABLE")
                .validFrom(LocalDateTime.now().minusYears(1))
                .validTo(LocalDateTime.now().minusDays(1))
                .build();

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(ticketRepository.findByTicketCode(ticketCode)).thenReturn(Optional.of(ticket));
        when(entryLogRepository.save(any(EntryLog.class))).thenReturn(null);

        VerifyResult result = ticketService.verifyTicket(ticketCode, "staff001", "device001");

        assertFalse(result.isSuccess());
        assertEquals("EXPIRED", result.getCode());
    }

    @Test
    void verifyTicket_Fail_MaxEntriesExceeded() {
        String ticketCode = "max-entries-ticket";
        Ticket ticket = Ticket.builder()
                .id(1L)
                .type(TicketType.YEAR_PASS)
                .ticketCode(ticketCode)
                .status("AVAILABLE")
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(1))
                .maxEntries(10)
                .usedEntries(10)
                .maxEntriesExceededAction("REJECT")
                .build();

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(true);
        when(ticketRepository.findByTicketCode(ticketCode)).thenReturn(Optional.of(ticket));
        when(entryLogRepository.save(any(EntryLog.class))).thenReturn(null);

        VerifyResult result = ticketService.verifyTicket(ticketCode, "staff001", "device001");

        assertFalse(result.isSuccess());
        assertEquals("MAX_ENTRIES_EXCEEDED", result.getCode());
    }

    @Test
    void verifyTicket_Fail_LockFailed() {
        String ticketCode = "locked-ticket";

        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
                .thenReturn(false);

        VerifyResult result = ticketService.verifyTicket(ticketCode, "staff001", "device001");

        assertFalse(result.isSuccess());
        assertEquals("LOCK_FAILED", result.getCode());
    }

    @Test
    void findValidPasses_Success() {
        String visitorId = "110101199001011234";
        Ticket ticket = Ticket.builder()
                .id(1L)
                .type(TicketType.YEAR_PASS)
                .ticketCode("ticket-code")
                .visitorId(visitorId)
                .visitorName("张三")
                .status("AVAILABLE")
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(1))
                .build();

        when(ticketRepository.findValidPasses(eq(visitorId), anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(ticket));

        List<TicketResponse> passes = ticketService.findValidPasses(visitorId);

        assertEquals(1, passes.size());
        assertEquals("张三", passes.get(0).getVisitorName());
    }

    @Test
    void createSingleTickets_Success() {
        SingleTicketCreateRequest request = SingleTicketCreateRequest.builder()
                .quantity(3)
                .performanceName("演唱会A")
                .channel("MEITUAN")
                .build();

        when(ticketRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Ticket> tickets = invocation.getArgument(0);
            long id = 1L;
            for (Ticket ticket : tickets) {
                ticket.setId(id++);
            }
            return tickets;
        });

        List<TicketResponse> tickets = ticketService.createSingleTickets(request);

        assertEquals(3, tickets.size());
        assertEquals(TicketType.SINGLE_USE, tickets.get(0).getType());
        assertEquals("演唱会A", tickets.get(0).getVisitorName());
        verify(ticketRepository).saveAll(anyList());
    }

    @Test
    void createSingleTickets_Fail_InvalidQuantity() {
        SingleTicketCreateRequest request = SingleTicketCreateRequest.builder()
                .quantity(0)
                .performanceName("演唱会A")
                .build();

        assertThrows(BusinessException.class, () -> ticketService.createSingleTickets(request));
    }
}