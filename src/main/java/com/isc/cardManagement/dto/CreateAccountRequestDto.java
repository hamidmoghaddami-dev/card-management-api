package com.isc.cardManagement.dto;


import com.isc.cardManagement.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequestDto {

        @NotBlank(message = "account.number.invalid")
        @Size(min = 10, max = 10, message = "{account.number.invalid}")
        @Pattern(regexp = "\\d+", message = "{account.number.invalid}")
        private String accountNumber;

        @NotNull(message = "{account.type.null}")
        private AccountType accountType;

        @NotBlank(message = "{national.code.invalid}")
        private String nationalCode;

}
