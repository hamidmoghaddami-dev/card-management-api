package com.isc.cardManagement.dto;

import com.isc.cardManagement.enums.AccountType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDto {
    private String accountNumber;
    private AccountType accountType;
}

