package com.isc.cardManagement.service;

import com.isc.cardManagement.dto.CardDto;
import com.isc.cardManagement.dto.CreateCardRequestDto;
import com.isc.cardManagement.dto.IssuerDto;
import com.isc.cardManagement.entity.AccountEntity;
import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.entity.IssuerEntity;
import com.isc.cardManagement.entity.PersonEntity;
import com.isc.cardManagement.enums.AccountType;
import com.isc.cardManagement.enums.CardType;
import com.isc.cardManagement.exception.BusinessException;
import com.isc.cardManagement.exception.NotFoundException;
import com.isc.cardManagement.repository.InMemoryRepository;
import com.isc.cardManagement.repository.jpa.AccountRepository;
import com.isc.cardManagement.repository.jpa.CardRepository;
import com.isc.cardManagement.repository.jpa.IssuerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("تست واحد CardService - createCard")
class CreateCardServiceTest {

    @Mock
    private InMemoryRepository inMemoryRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private IssuerRepository issuerRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private CardDto validRequest;
    private AccountEntity mockAccount;
    private IssuerEntity mockIssuer;
    private CardEntity mockCard;

    @BeforeEach
    void setUp() {
        // Setup Person
        PersonEntity mockPerson = PersonEntity.builder()
                .id(1L)
                .nationalCode("1234567890")
                .firstName("علی")
                .lastName("احمدی")
                .phone("09121234567")
                .address("تهران")
                .build();

        // Setup Account
        mockAccount = AccountEntity.builder()
                .id(1L)
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .owner(mockPerson)
                .build();

        // Setup Issuer
        mockIssuer = IssuerEntity.builder()
                .id(1L)
                .issuerCode("603799")
                .name("بانک ملی")
                .build();

        // Setup Card
        mockCard = CardEntity.builder()
                .id(1L)
                .cardNumber("6037997711223344")
                .cardType(CardType.DEBIT)
                .account(mockAccount)
                .issuer(mockIssuer)
                .expirationMonth("12")
                .expirationYear("1405")
                .active(true)
                .build();

        // Setup Request DTO
        CardDto cardDto = CardDto.builder()
                .cardNumber("6037997711223344")
                .expirationMonth("12")
                .expirationYear("1405")
                .cardType(CardType.DEBIT)
                .issuerCode("603799")
                .accountNumber("1234567890")
                .build();

        validRequest = cardDto;
    }

    @Test
    @DisplayName("ایجاد کارت موفق - سناریو کامل")
    void createCard_Success() {
        // Given
        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));

        when(inMemoryRepository.getCardsByNationalCode("1234567890"))
                .thenReturn(Set.of()); // هیچ کارت قبلی وجود ندارد

        when(issuerRepository.findByIssuerCode("603799"))
                .thenReturn(Optional.of(mockIssuer));

        when(cardRepository.saveAndFlush(any(CardEntity.class)))
                .thenReturn(mockCard);

        when(inMemoryRepository.saveCard(any(CardEntity.class)))
                .thenReturn(mockCard);
        // When
        CardDto result = cardService.createCard(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCardNumber()).isEqualTo("6037997711223344");
        assertThat(result.getExpirationMonth()).isEqualTo("12");
        assertThat(result.getExpirationYear()).isEqualTo("1405");
        assertThat(result.getCardType()).isEqualTo(CardType.DEBIT);
        assertThat(result.isActive()).isTrue();

        // Verify interactions
        verify(accountRepository, times(2)).findByAccountNumber("1234567890");
        verify(inMemoryRepository).getCardsByNationalCode("1234567890");
        verify(issuerRepository).findByIssuerCode("603799");
        verify(cardRepository).saveAndFlush(argThat(card ->
                card.getCardNumber().equals("6037997711223344") &&
                        card.getCardType() == CardType.DEBIT &&
                        card.isActive()
        ));
        verify(inMemoryRepository).saveCard(any(CardEntity.class));
    }

    @Test
    @DisplayName("خطا: حساب یافت نشد - مرحله اول")
    void createCard_AccountNotFound_FirstCheck() {
        // Given
        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cardService.createCard(validRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("حساب یافت نشد");

        // Verify که فقط یک بار جستجو شد
        verify(accountRepository, times(1)).findByAccountNumber("1234567890");
        verify(inMemoryRepository, never()).getCardsByNationalCode(anyString());
        verify(cardRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("خطا: کارت تکراری در Cache")
    void createCard_DuplicateCardInCache() {
        // Given
        CardEntity existingCard = CardEntity.builder()
                .id(99L)
                .cardNumber("6037997711223344") // همان شماره کارت
                .build();

        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));

        when(inMemoryRepository.getCardsByNationalCode("1234567890"))
                .thenReturn(Set.of(existingCard)); // کارت قبلی با همان شماره

        // When & Then
        assertThatThrownBy(() -> cardService.createCard(validRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("شماره کارت تکراری: 6037997711223344");

        verify(accountRepository, times(1)).findByAccountNumber("1234567890");
        verify(inMemoryRepository).getCardsByNationalCode("1234567890");
        verify(cardRepository, never()).saveAndFlush(any());
        verify(inMemoryRepository, never()).saveCard(any());
    }

    @Test
    @DisplayName("خطا: صادرکننده یافت نشد")
    void createCard_IssuerNotFound() {
        // Given
        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));

        when(inMemoryRepository.getCardsByNationalCode("1234567890"))
                .thenReturn(Set.of());

        when(issuerRepository.findByIssuerCode("603799"))
                .thenReturn(Optional.empty()); // صادرکننده یافت نشد

        // When & Then
        assertThatThrownBy(() -> cardService.createCard(validRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("صادرکننده یافت نشد");

        verify(accountRepository, times(2)).findByAccountNumber("1234567890");
        verify(issuerRepository).findByIssuerCode("603799");
        verify(cardRepository, never()).saveAndFlush(any());
        verify(inMemoryRepository, never()).saveCard(any());
    }

    @Test
    @DisplayName("بررسی عدم تکراری بودن - کارت‌های دیگر در Cache")
    void createCard_WithOtherCardsInMemory_Success() {
        // Given
        CardEntity otherCard1 = CardEntity.builder()
                .id(2L)
                .cardNumber("6037991122334455") // شماره متفاوت
                .build();

        CardEntity otherCard2 = CardEntity.builder()
                .id(3L)
                .cardNumber("6037995566778899") // شماره متفاوت
                .build();

        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));

        when(inMemoryRepository.getCardsByNationalCode("1234567890"))
                .thenReturn(Set.of(otherCard1, otherCard2)); // کارت‌های دیگر

        when(issuerRepository.findByIssuerCode("603799"))
                .thenReturn(Optional.of(mockIssuer));

        when(cardRepository.saveAndFlush(any(CardEntity.class)))
                .thenReturn(mockCard);

        when(inMemoryRepository.saveCard(any(CardEntity.class)))
                .thenReturn(mockCard);

        // When
        CardDto result = cardService.createCard(validRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCardNumber()).isEqualTo("6037997711223344");

        verify(cardRepository).saveAndFlush(any(CardEntity.class));
        verify(inMemoryRepository).saveCard(any(CardEntity.class));
    }


    @Test
    @DisplayName("بررسی ذخیره در InMemory بعد از Database")
    void createCard_SavesInMemoryAfterDatabase() {
        // Given
        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));

        when(inMemoryRepository.getCardsByNationalCode("1234567890"))
                .thenReturn(Set.of());

        when(issuerRepository.findByIssuerCode("603799"))
                .thenReturn(Optional.of(mockIssuer));

        when(cardRepository.saveAndFlush(any(CardEntity.class)))
                .thenReturn(mockCard);

        // تغییر از doNothing به when().thenReturn()
        when(inMemoryRepository.saveCard(any(CardEntity.class)))
                .thenReturn(mockCard);

        // When
        cardService.createCard(validRequest);

        // Then - بررسی ترتیب فراخوانی
        var inOrder = inOrder(cardRepository, inMemoryRepository);
        inOrder.verify(cardRepository).saveAndFlush(any(CardEntity.class));
        inOrder.verify(inMemoryRepository).saveCard(any(CardEntity.class));
    }


    @Test
    @DisplayName("بررسی مقادیر ذخیره شده در Entity")
    void createCard_VerifyEntityValues() {
        // Given
        when(accountRepository.findByAccountNumber("1234567890"))
                .thenReturn(Optional.of(mockAccount));

        when(inMemoryRepository.getCardsByNationalCode("1234567890"))
                .thenReturn(Set.of());

        when(issuerRepository.findByIssuerCode("603799"))
                .thenReturn(Optional.of(mockIssuer));

        when(cardRepository.saveAndFlush(any(CardEntity.class)))
                .thenReturn(mockCard);

        // اگر saveCard مقداری برمی‌گرداند (مثلا CardEntity یا boolean)
        when(inMemoryRepository.saveCard(any(CardEntity.class)))
                .thenReturn(mockCard); // یا true اگر boolean برمی‌گرداند

        // When
        cardService.createCard(validRequest);

        // Then - بررسی دقیق مقادیر Entity
        verify(cardRepository).saveAndFlush(argThat(card -> {
            assertThat(card.getCardNumber()).isEqualTo("6037997711223344");
            assertThat(card.getCardType()).isEqualTo(CardType.DEBIT);
            assertThat(card.getExpirationMonth()).isEqualTo("12");
            assertThat(card.getExpirationYear()).isEqualTo("1405");
            assertThat(card.isActive()).isTrue();
            assertThat(card.getAccount()).isEqualTo(mockAccount);
            assertThat(card.getIssuer()).isEqualTo(mockIssuer);
            return true;
        }));
    }
}

