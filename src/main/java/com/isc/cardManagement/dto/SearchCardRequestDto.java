package com.isc.cardManagement.dto;

import com.isc.cardManagement.enums.CardType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCardRequestDto {


        @NotBlank(message = "{national.code.invalid}")
        private String nationalCode;

        private CardType cardType; // اختیاری

        private String issuerCode; // اختیاری
}

