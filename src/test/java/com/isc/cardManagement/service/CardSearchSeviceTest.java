package com.isc.cardManagement.service;

import com.isc.cardManagement.dto.CardResponseDto;
import com.isc.cardManagement.dto.CardSearchDto;
import com.isc.cardManagement.dto.PagedResponseDto;
import com.isc.cardManagement.entity.AccountEntity;
import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.entity.IssuerEntity;
import com.isc.cardManagement.entity.PersonEntity;
import com.isc.cardManagement.enums.CardType;
import com.isc.cardManagement.repository.jpa.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardServiceImpl.search Unit Tests")
class CardSearchServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardSearchService;

    private CardEntity testCard;
    private IssuerEntity testIssuer;
    private AccountEntity testAccount;

    @BeforeEach
    void setUp() {
        PersonEntity testPerson = PersonEntity.builder()
                .id(1L)
                .nationalCode("1234567890")
                .firstName("علی")
                .lastName("احمدی")
                .build();

        testIssuer = IssuerEntity.builder()
                .id(1L)
                .issuerCode("627353")
                .name("بانک تجارت")
                .build();

        testAccount = AccountEntity.builder()
                .id(1L)
                .accountNumber("1234567890")
                .owner(testPerson)
                .build();

        testCard = CardEntity.builder()
                .id(1L)
                .cardNumber("6273531234567890")
                .cardType(CardType.DEBIT)
                .active(true)
                .issuer(testIssuer)
                .account(testAccount)
                .expirationMonth("12")
                .expirationYear("2025")
                .build();
    }

    // ==================== تست‌های موفقیت ====================

    @Test
    @DisplayName("جستجو با کد ملی - موفقیت")
    void searchCards_WithNationalCode_Success() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .nationalCode("1234567890")
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.searchCards(
                eq("1234567890"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCardNumber()).isEqualTo("6273531234567890");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(0);

        verify(cardRepository).searchCards(
                eq("1234567890"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                argThat(pageable -> pageable.isUnpaged())
        );
    }

    @Test
    @DisplayName("جستجو با شماره کارت - موفقیت")
    void searchCards_WithCardNumber_Success() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .cardNumber("6273531234567890")
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.searchCards(
                isNull(),
                eq("6273531234567890"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCardNumber()).isEqualTo("6273531234567890");
    }

    @Test
    @DisplayName("جستجو با صفحه‌بندی و مرتب‌سازی صعودی - موفقیت")
    void searchCards_WithPaginationAndAscSort_Success() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .active(true)
                .page(0)
                .size(10)
                .sortBy("cardNumber")
                .sortDirection("ASC")
                .build();

        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "cardNumber"));
        Page<CardEntity> mockPage = new PageImpl<>(
                List.of(testCard),
                expectedPageable,
                1
        );

        when(cardRepository.searchCards(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                isNull(),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();

        verify(cardRepository).searchCards(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                isNull(),
                argThat(pageable ->
                        pageable.getPageNumber() == 0 &&
                                pageable.getPageSize() == 10 &&
                                pageable.getSort().getOrderFor("cardNumber") != null &&
                                pageable.getSort().getOrderFor("cardNumber").getDirection() == Sort.Direction.ASC
                )
        );
    }

    @Test
    @DisplayName("جستجوی موفق با صفحه‌بندی و مرتب‌سازی نزولی")
    void searchCards_WithPaginationAndDescSort_Success() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .cardNumber("6037")
                .page(0)
                .size(10)
                .sortBy("cardNumber")
                .sortDirection("DESC")
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(
                List.of(testCard),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "cardNumber")),
                1
        );

        when(cardRepository.searchCards(
                isNull(),           // nationalCode
                eq("6037"),    // cardNumber
                isNull(),           // issuerName
                isNull(),           // cardType
                isNull(),           // accountType
                isNull(),           // active
                any(Pageable.class) // pageable
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();

        verify(cardRepository).searchCards(
                isNull(),     // nationalCode
                eq("6037"),   // cardNumber
                isNull(),     // issuerName
                isNull(),     // cardType
                isNull(),     // accountType
                isNull(),     // active
                argThat(pageable -> {
                    Sort.Order order = pageable.getSort().getOrderFor("cardNumber");
                    return order != null && order.getDirection() == Sort.Direction.DESC;
                })
        );
    }



    @Test
    @DisplayName("جستجو با چند فیلتر - موفقیت")
    void searchCards_WithMultipleFilters_Success() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .nationalCode("1234567890")
                .issuerCode("627353")
                .cardType(CardType.DEBIT)
                .active(true)
                .accountNumber("1234567890")
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.searchCards(
                eq("1234567890"),
                isNull(),
                eq("627353"),
                eq(CardType.DEBIT),
                eq(true),
                eq("1234567890"),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        CardResponseDto dto = result.getContent().get(0);
        assertThat(dto.getCardType()).isEqualTo(CardType.DEBIT);
        assertThat(dto.isActive()).isTrue();
        assertThat(dto.getIssuer().getIssuerCode()).isEqualTo("627353");
    }

    @Test
    @DisplayName("جستجو بدون page و size - استفاده از Unpaged")
    void searchCards_WithoutPageAndSize_UsesUnpaged() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .cardType(CardType.CREDIT)
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.searchCards(
                isNull(),
                isNull(),
                isNull(),
                eq(CardType.CREDIT),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();

        verify(cardRepository).searchCards(
                isNull(),
                isNull(),
                isNull(),
                eq(CardType.CREDIT),
                isNull(),
                isNull(),
                argThat(Pageable::isUnpaged)
        );
    }

    @Test
    @DisplayName("جستجو با page بدون size - استفاده از Unpaged")
    void searchCards_WithPageWithoutSize_UsesUnpaged() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .page(0)
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.searchCards(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        verify(cardRepository).searchCards(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                argThat(Pageable::isUnpaged)
        );
    }

    @Test
    @DisplayName("جستجو با sortBy خالی - استفاده از id پیش‌فرض")
    void searchCards_WithNullSortBy_UsesDefaultSort() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .page(0)
                .size(10)
                .sortDirection("ASC")
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.searchCards(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();

        verify(cardRepository).searchCards(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                argThat(pageable -> {
                    Sort.Order order = pageable.getSort().getOrderFor("id");
                    return order != null && order.getDirection() == Sort.Direction.ASC;
                })
        );
    }

    @Test
    @DisplayName("جستجو با sortDirection خالی - استفاده از ASC پیش‌فرض")
    void searchCards_WithNullSortDirection_UsesAscSort() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .page(0)
                .size(10)
                .sortBy("cardNumber")
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.searchCards(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();

        verify(cardRepository).searchCards(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                argThat(pageable ->
                        pageable.getSort().getOrderFor("cardNumber").getDirection() == Sort.Direction.ASC
                )
        );
    }

    // ==================== تست‌های شکست/حالات خاص ====================

    @Test
    @DisplayName("جستجو بدون نتیجه - لیست خالی")
    void searchCards_NoResults_ReturnsEmptyList() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .nationalCode("9999999999")
                .build();

        Page<CardEntity> emptyPage = new PageImpl<>(Collections.emptyList());

        when(cardRepository.searchCards(
                eq("9999999999"),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(emptyPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("جستجو با DTO خالی - تمام کارت‌ها")
    void searchCards_EmptyDto_ReturnsAllCards() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder().build();

        Page<CardEntity> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.searchCards(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(cardRepository).searchCards(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                argThat(Pageable::isUnpaged)
        );
    }

    @Test
    @DisplayName("جستجو با چند کارت - موفقیت")
    void searchCards_MultipleCards_Success() {
        // Given
        CardEntity card2 = CardEntity.builder()
                .id(2L)
                .cardNumber("6273531234567891")
                .cardType(CardType.CREDIT)
                .active(true)
                .issuer(testIssuer)
                .account(testAccount)
                .expirationMonth("06")
                .expirationYear("2026")
                .build();

        CardSearchDto searchDto = CardSearchDto.builder()
                .active(true)
                .page(0)
                .size(10)
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(
                List.of(testCard, card2),
                PageRequest.of(0, 10),
                2
        );

        when(cardRepository.searchCards(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(true),
                isNull(),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(CardResponseDto::getCardNumber)
                .containsExactlyInAnyOrder("6273531234567890", "6273531234567891");
    }

    @Test
    @DisplayName("جستجو با فیلتر active=false - موفقیت")
    void searchCards_WithInactiveFilter_Success() {
        // Given
        testCard.setActive(false);

        CardSearchDto searchDto = CardSearchDto.builder()
                .active(false)
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.searchCards(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(false),
                isNull(),
                any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("جستجو در صفحه دوم - موفقیت")
    void searchCards_SecondPage_Success() {
        // Given
        CardSearchDto searchDto = CardSearchDto.builder()
                .page(1)
                .size(10)
                .build();

        Page<CardEntity> mockPage = new PageImpl<>(
                List.of(testCard),
                PageRequest.of(1, 10),
                15 // total 15 items
        );

        when(cardRepository.searchCards(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
        )).thenReturn(mockPage);

        // When
        PagedResponseDto<CardResponseDto> result = cardSearchService.searchCards(searchDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

}

