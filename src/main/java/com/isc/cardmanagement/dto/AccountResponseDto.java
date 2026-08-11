package com.isc.cardmanagement.dto;

import com.isc.cardmanagement.enums.AccountType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDto {
    private String accountNumber;
    private AccountType accountType;
}

