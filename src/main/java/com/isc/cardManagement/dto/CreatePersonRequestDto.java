package com.isc.cardManagement.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePersonRequestDto {

        @NotBlank(message = "national.code.invalid")
        @Size(min = 10, max = 10, message = "national.code.invalid")
        @Pattern(regexp = "\\d+", message = "national.code.invalid")
        private String nationalCode;

        @NotBlank(message = "first.name.blank")
        @Size(min = 2, max = 50)
        private String firstName;

        @NotBlank(message = "last.name.blank")
        @Size(min = 2, max = 50)
        private String lastName;

        @NotBlank(message = "phone.invalid")
        @Pattern(regexp = "\\d{11}", message = "phone.invalid")
        private String phone;

        @NotBlank(message = "address.blank")
        @Size(max = 255)
        private String address;

}
