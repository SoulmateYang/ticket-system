package com.scenic.ticket.service;

import com.scenic.ticket.dto.OtaOrderSyncRequest;
import com.scenic.ticket.dto.SingleTicketCreateRequest;
import com.scenic.ticket.dto.TicketResponse;
import com.scenic.ticket.exception.BusinessException;
import com.scenic.ticket.model.OtaChannel;
import com.scenic.ticket.model.OtaOrder;
import com.scenic.ticket.model.TicketType;
import com.scenic.ticket.repository.OtaOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtaOrderServiceTest {

    @Mock
    private OtaOrderRepository otaOrderRepository;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private OtaOrderService otaOrderService;

    @Test
    void syncOrder_Success() {
        OtaOrderSyncRequest request = OtaOrderSyncRequest.builder()
                .channel(OtaChannel.MEITUAN)
                .externalOrderId("MT20230526001")
                .performanceName("演唱会A")
                .quantity(2)
                .amount(400.0)
                .buyerName("李四")
                .buyerPhone("13900139000")
                .build();

        when(otaOrderRepository.findByExternalOrderId("MT20230526001")).thenReturn(Optional.empty());
        when(otaOrderRepository.save(any(OtaOrder.class))).thenAnswer(invocation -> {
            OtaOrder order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        var response = otaOrderService.syncOrder(request);

        assertNotNull(response);
        assertEquals(OtaChannel.MEITUAN, response.getOtaChannel());
        assertEquals("MT20230526001", response.getExternalOrderId());
        assertEquals("SYNCED", response.getSyncStatus());
        verify(otaOrderRepository).save(any(OtaOrder.class));
    }

    @Test
    void syncOrder_ThrowsException_WhenDuplicate() {
        OtaOrderSyncRequest request = OtaOrderSyncRequest.builder()
                .channel(OtaChannel.DOUYIN)
                .externalOrderId("DY20230526001")
                .quantity(1)
                .build();

        when(otaOrderRepository.findByExternalOrderId("DY20230526001"))
                .thenReturn(Optional.of(new OtaOrder()));

        assertThrows(BusinessException.class, () -> otaOrderService.syncOrder(request));
    }

    @Test
    void getPendingOrdersByChannel_Success() {
        OtaOrder order = OtaOrder.builder()
                .id(1L)
                .otaChannel(OtaChannel.CTRIP)
                .externalOrderId("CT20230526001")
                .ticketType(TicketType.OTA_TICKET)
                .quantity(1)
                .syncStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(otaOrderRepository.findByOtaChannelAndSyncStatus(OtaChannel.CTRIP, "PENDING"))
                .thenReturn(List.of(order));

        var orders = otaOrderService.getPendingOrdersByChannel(OtaChannel.CTRIP);

        assertEquals(1, orders.size());
        assertEquals(OtaChannel.CTRIP, orders.get(0).getOtaChannel());
    }

    @Test
    void generateTicketsFromOrder_Success() {
        OtaOrder order = OtaOrder.builder()
                .id(1L)
                .otaChannel(OtaChannel.MEITUAN)
                .externalOrderId("MT20230526001")
                .ticketType(TicketType.OTA_TICKET)
                .performanceName("演唱会B")
                .quantity(3)
                .syncStatus("SYNCED")
                .createdAt(LocalDateTime.now())
                .build();

        TicketResponse ticket = TicketResponse.builder()
                .id(1L)
                .type(TicketType.SINGLE_USE)
                .ticketCode("ticket-code")
                .build();

        when(otaOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(ticketService.createSingleTickets(any(SingleTicketCreateRequest.class)))
                .thenReturn(List.of(ticket, ticket, ticket));
        when(otaOrderRepository.save(any(OtaOrder.class))).thenReturn(order);

        var tickets = otaOrderService.generateTicketsFromOrder(1L);

        assertEquals(3, tickets.size());
        verify(ticketService).createSingleTickets(any(SingleTicketCreateRequest.class));
    }

    @Test
    void generateTicketsFromOrder_ThrowsException_WhenOrderNotSynced() {
        OtaOrder order = OtaOrder.builder()
                .id(1L)
                .otaChannel(OtaChannel.MEITUAN)
                .externalOrderId("MT20230526001")
                .syncStatus("PENDING")
                .build();

        when(otaOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> otaOrderService.generateTicketsFromOrder(1L));
    }
}