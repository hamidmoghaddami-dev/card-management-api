package com.isc.cardManagement.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponseDTO {
    private String error;
    private String details;
    private LocalDateTime timestamp;

    public ErrorResponseDTO(String error) {
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDTO(String error, String details) {
        this.error = error;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}


