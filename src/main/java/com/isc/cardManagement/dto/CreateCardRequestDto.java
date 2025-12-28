package com.isc.cardManagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCardRequestDto {

    @NotNull(message = "اطلاعات کارت الزامی است")
    @Valid
    CardDto cardDto;

    @Valid
    IssuerDto issuerDto;

    @Valid
    AccountDto accountDto;

}
