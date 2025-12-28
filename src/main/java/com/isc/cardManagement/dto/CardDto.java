package com.isc.cardManagement.dto;


import com.isc.cardManagement.enums.CardType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDto {

    @NotBlank(message = "card.number.invalid")
    @Size(min = 16, max = 16, message = "{card.number.invalid}")
    @Pattern(regexp = "\\d+", message = "{card.number.invalid}")
    private String cardNumber;

    @NotBlank(message = "expiration.month.invalid")
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "expiration.month.invalid")
    private String expirationMonth;

    @NotBlank(message = "expiration.year.invalid")
    private String expirationYear;

    private boolean active;

    @NotNull(message = "card.type.null")
    private CardType cardType;

    @NotNull(message = "issuer.code.invalid")
    private String issuerCode;

    @NotNull(message = "account.number.invalid")
    private String accountNumber;
}

