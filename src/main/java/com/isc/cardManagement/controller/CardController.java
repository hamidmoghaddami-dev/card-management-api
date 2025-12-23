package com.isc.cardManagement.controller;

import com.isc.cardManagement.dto.CardDto;
import com.isc.cardManagement.dto.CardResponseDto;
import com.isc.cardManagement.dto.CreateCardRequestDto;
import com.isc.cardManagement.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Card Management", description = "APIs for managing cards")
public class CardController {


    private final CardService cardService;


    @Operation(
            summary = "Get card details",
            description = "Fetch the details of a specific card by card number",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved card details"),
                    @ApiResponse(responseCode = "404", description = "Card not found")
            }
    )
    @GetMapping("/{nationalCode}")
    public ResponseEntity<List<CardResponseDto>> getCardsByNationalCode(
            @PathVariable String nationalCode) {

        List<CardResponseDto> cards = cardService.getCardsByNationalCode(nationalCode);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "افزودن کارت جدید", description = "با استفاده از این سرویس کارت جدیدی برای شخص ثبت می شود")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "کارت با موفقیت اضافه شد."),
            @ApiResponse(responseCode = "400", description = "خطا در داده های ورودی"),
            @ApiResponse(responseCode = "500", description = "خطای داخلی سرور.")
    })
    @PostMapping
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequestDto dto)
            throws BadRequestException {

        log.info("POST /api/cards - cardNumber: {}", dto.getCardDto().getCardNumber());

        CardDto created = cardService.createCard(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

}
