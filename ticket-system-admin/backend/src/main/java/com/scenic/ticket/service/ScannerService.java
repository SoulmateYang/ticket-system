package com.scenic.ticket.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 扫码枪事件监听服务
 * 处理USB HID设备的插入和拔出事件
 */
@Slf4j
@Service
public class ScannerService {

    /**
     * 扫码枪扫码事件
     */
    public interface ScanEvent {
        String getDeviceId();
        String getTicketCode();
    }

    /**
     * 处理扫码结果
     */
    public void handleScanResult(String ticketCode, String deviceId) {
        log.info("扫码结果: ticketCode={}, deviceId={}", ticketCode, deviceId);
    }

    /**
     * 设备插入事件
     */
    @EventListener
    public void onDeviceConnected(ScannerDeviceEvent event) {
        log.info("扫码枪已连接: deviceId={}, name={}", event.getDeviceId(), event.getDeviceName());
    }

    /**
     * 设备拔出事件
     */
    @EventListener
    public void onDeviceDisconnected(ScannerDeviceEvent event) {
        log.info("扫码枪已断开: deviceId={}, name={}", event.getDeviceId(), event.getDeviceName());
    }

    /**
     * 扫码枪设备事件
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ScannerDeviceEvent {
        private String deviceId;
        private String deviceName;
        private String eventType;
    }
}