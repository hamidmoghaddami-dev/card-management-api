package com.isc.cardmanagement.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonResponseDto {
    private String nationalCode;
    private String fullName;
    private String phone;
    private String address;
}

