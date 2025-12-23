package com.isc.cardManagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonDto {

    @NotBlank(message = "{national.code.invalid}")
    @Size(min = 10, max = 10, message = "{national.code.invalid}")
    @Pattern(regexp = "\\d+", message = "{national.code.invalid}")
    private String nationalCode;

    @NotBlank(message = "{first.name.blank}")
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank(message = "{last.name.blank}")
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank(message = "{phone.invalid}")
    @Pattern(regexp = "\\d{11}", message = "{phone.invalid}")
    private String phone;

    @NotBlank(message = "{address.blank}")
    @Size(max = 255)
    private String address;
}
