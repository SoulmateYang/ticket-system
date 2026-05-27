package com.scenic.ticket.exception;

public class StaffNotFoundException extends BusinessException {

    public StaffNotFoundException(Long id) {
        super("STAFF_NOT_FOUND", "员工不存在: id=" + id);
    }

    public StaffNotFoundException(String message) {
        super("STAFF_NOT_FOUND", message);
    }
}