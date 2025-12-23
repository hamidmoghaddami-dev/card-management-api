package com.isc.cardManagement.dto;

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
    CardDto cardDto;

    IssuerDto issuerDto;

    AccountDto accountDto;

    PersonDto personDto;
}
