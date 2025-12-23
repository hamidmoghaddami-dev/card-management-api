package com.isc.cardManagement.service;

import com.isc.cardManagement.dto.CardResponseDto;
import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.enums.CardType;
import com.isc.cardManagement.exception.NotFoundException;
import com.isc.cardManagement.repository.InMemoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class)
@SpringBootTest
class CardServiceTest {

    @MockBean
    private InMemoryRepository repository;

    @SpyBean
    private CardService cardService;

    @Test
    @DisplayName("باید کارت‌های یک شخص را برگرداند")
    void shouldReturnCardsForValidNationalCode() {
        // Given
        String nationalCode = "1234567890";
        List<CardEntity> cards = List.of(
                CardEntity.builder()
                        .cardNumber("6273539876543210")
                        .cardType(CardType.CREDIT)
                        .build()
        );

        when(repository.findCardsByNationalCode(nationalCode))
                .thenReturn(cards);

        // When
        List<CardResponseDto> result = cardService.getCardsByNationalCode(nationalCode);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCardNumber()).isEqualTo("6273539876543210");
        verify(repository, times(1)).findCardsByNationalCode(nationalCode);
    }

    @Test
    @DisplayName("باید خطای NotFound برای کد ملی نامعتبر پرتاب کند")
    void shouldThrowNotFoundForInvalidNationalCode() {
        // Given
        String nationalCode = "0000000000";
        when(repository.findCardsByNationalCode(nationalCode))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> cardService.getCardsByNationalCode(nationalCode))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("برای کد ملی");
    }
}

