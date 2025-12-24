package com.isc.cardManagement.dto;

import com.isc.cardManagement.enums.CardType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "پارامترهای جستجوی کارت - همه فیلدها اختیاری هستند")
public class CardSearchDto {

    @Schema(description = "کد ملی صاحب کارت", example = "1234567890")
    @Size(min = 10, max = 10, message = "کد ملی باید 10 رقم باشد")
    @Pattern(regexp = "^\\d{10}$", message = "کد ملی باید فقط شامل اعداد باشد")
    private String nationalCode;

    @Schema(description = "شماره کارت", example = "6273539876543210")
    @Size(min = 16, max = 16, message = "شماره کارت باید 16 رقم باشد")
    @Pattern(regexp = "^\\d{16}$", message = "شماره کارت باید فقط شامل اعداد باشد")
    private String cardNumber;

    @Schema(description = "کد صادرکننده", example = "627353")
    @Size(min = 6, max = 6, message = "کد صادرکننده باید 6 رقم باشد")
    @Pattern(regexp = "^\\d{6}$", message = "کد صادرکننده باید فقط شامل اعداد باشد")
    private String issuerCode;

    @Schema(description = "نوع کارت", example = "CREDIT", allowableValues = {"CREDIT", "DEBIT"})
    private CardType cardType;

    @Schema(description = "وضعیت فعال بودن کارت", example = "true")
    private Boolean active;

    @Schema(description = "شماره حساب", example = "1234567890")
    @Size(min = 10, max = 10, message = "شماره حساب باید 10 رقم باشد")
    @Pattern(regexp = "^\\d{10}$", message = "شماره حساب باید فقط شامل اعداد باشد")
    private String accountNumber;

    @Schema(description = "شماره صفحه (از صفر شروع می شود)", example = "0", minimum = "0")
    private Integer page;

    @Schema(description = "تعداد ایتم های هر صفحه", example = "10", minimum = "1")
    private Integer size;

    @Schema(description = "فیلد مرتب سازی", example = "cardNumber", defaultValue = "id")
    @Builder.Default
    private String sortBy = "id";

    @Schema(description = "جهت مرتب سازی", example = "ASC", allowableValues = {"ASC", "DESC"}, defaultValue = "ASC")
    @Builder.Default
    private String sortDirection = "ASC";
}
