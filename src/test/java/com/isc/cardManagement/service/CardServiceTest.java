package com.isc.cardManagement.service;

import com.isc.cardManagement.dto.CardResponseDto;
import com.isc.cardManagement.entity.AccountEntity;
import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.entity.IssuerEntity;
import com.isc.cardManagement.entity.PersonEntity;
import com.isc.cardManagement.enums.AccountType;
import com.isc.cardManagement.enums.CardType;
import com.isc.cardManagement.exception.NotFoundException;
import com.isc.cardManagement.repository.InMemoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private InMemoryRepository repository;


    @InjectMocks
    private CardServiceImpl cardService;

    private IssuerEntity testIssuer;
    private AccountEntity testAccount;

    @BeforeEach
    void setUp() {
        PersonEntity testPerson = PersonEntity.builder()
                .id(1L)
                .nationalCode("1234567890")
                .firstName("علی")
                .lastName("احمدی")
                .phone("09121234567")
                .address("تهران - خیابان ولیعصر")
                .build();

        testIssuer = IssuerEntity.builder()
                .id(1L)
                .issuerCode("627353")
                .name("بانک تجارت")
                .build();

        testAccount = AccountEntity.builder()
                .id(1L)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .owner(testPerson)
                .build();
    }

    @Test
    @DisplayName("باید کارت‌های یک شخص را برگرداند")
    void shouldReturnCardsForValidNationalCode() {
        // Given
        String nationalCode = "1234567890";

        CardEntity card = CardEntity.builder()
                .id(1L)
                .cardNumber("6273539876543210")
                .cardType(CardType.CREDIT)
                .expirationMonth("06")
                .expirationYear("1406")
                .active(true)
                .account(testAccount)
                .issuer(testIssuer)
                .build();

        when(repository.getCardsByNationalCode(nationalCode))
                .thenReturn(Set.of(card));

        // When
        List<CardResponseDto> result = cardService.getCardsByNationalCode(nationalCode);

        // Then
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getCardNumber()).isEqualTo("6273539876543210");
        assertThat(result.get(0).getCardType()).isEqualTo(CardType.CREDIT);

        verify(repository, times(1)).getCardsByNationalCode(nationalCode);
    }

    @Test
    @DisplayName("باید خطای NotFound برای کد ملی بدون کارت پرتاب کند")
    void shouldThrowNotFoundForInvalidNationalCode() {
        // Given
        String nationalCode = "0000000000";

        when(repository.getCardsByNationalCode(nationalCode))
                .thenReturn(Collections.emptySet());

        // When & Then
        assertThatThrownBy(() -> cardService.getCardsByNationalCode(nationalCode))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("کارتی برای کد ملی")
                .hasMessageContaining(nationalCode);

        verify(repository, times(1)).getCardsByNationalCode(nationalCode);
    }

}

