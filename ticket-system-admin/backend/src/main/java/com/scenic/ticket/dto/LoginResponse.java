package com.scenic.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private StaffDto staff;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffDto {
        private Long id;
        private String employeeNo;
        private String name;
        private String role;
    }
}