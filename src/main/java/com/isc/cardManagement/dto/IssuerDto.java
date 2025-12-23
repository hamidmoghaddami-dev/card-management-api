package com.isc.cardManagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuerDto {

    @NotBlank(message = "{issuer.code.invalid}")
    @Size(min = 6, max = 6, message = "{issuer.code.invalid}")
    @Pattern(regexp = "\\d+", message = "{issuer.code.invalid}")
    private String issuerCode;

    @NotBlank(message = "{issuer.name.blank}")
    @Size(max = 100)
    private String name;
}

