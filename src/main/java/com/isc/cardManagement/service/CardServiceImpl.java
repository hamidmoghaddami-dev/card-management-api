package com.isc.cardManagement.service;

import com.isc.cardManagement.dto.*;
import com.isc.cardManagement.entity.AccountEntity;
import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.entity.IssuerEntity;
import com.isc.cardManagement.exception.BadRequestException;
import com.isc.cardManagement.exception.BusinessException;
import com.isc.cardManagement.exception.NotFoundException;
import com.isc.cardManagement.mapper.CardMapper;
import com.isc.cardManagement.repository.InMemoryRepository;
import com.isc.cardManagement.repository.jpa.AccountRepository;
import com.isc.cardManagement.repository.jpa.CardRepository;
import com.isc.cardManagement.repository.jpa.IssuerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final InMemoryRepository inMemoryRepository;
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final IssuerRepository issuerRepository;


    @Override
    @Transactional(readOnly = true)
    public List<CardResponseDto> getCardsByNationalCode(String nationalCode) {

        Set<CardEntity> cards = inMemoryRepository.getCardsByNationalCode(nationalCode);

        if (cards.isEmpty()) {
            throw new NotFoundException("کارتی برای کد ملی " + nationalCode + " یافت نشد");
        }

        return cards.stream()
                .map(CardResponseDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public CardDto createCard(CreateCardRequestDto dto) throws BadRequestException {

        log.info("Creating card: {}", dto.getCardDto().getCardNumber());

        String ownerNationalCode = accountRepository
                .findByAccountNumber(dto.getCardDto().getAccountNumber())
                .map(acc -> acc.getOwner().getNationalCode())
                .orElseThrow(() -> new NotFoundException("حساب یافت نشد"));

        Set<CardEntity> existingCards = inMemoryRepository
                .getCardsByNationalCode(ownerNationalCode);

        boolean duplicateInCache = existingCards.stream()
                .anyMatch(card -> card.getCardNumber().equals(dto.getCardDto().getCardNumber()));

        if (duplicateInCache) {
            throw new BusinessException(
                    String.format("شماره کارت تکراری: %s", dto.getCardDto().getCardNumber()));
        }

        AccountEntity account = accountRepository
                .findByAccountNumber(dto.getCardDto().getAccountNumber())
                .orElseThrow(() -> new NotFoundException("حساب یافت نشد"));

        IssuerEntity issuer = issuerRepository
                .findByIssuerCode(dto.getIssuerDto().getIssuerCode())
                .orElseThrow(() -> new NotFoundException("صادرکننده یافت نشد"));

        CardEntity card = CardEntity.builder()
                .cardNumber(dto.getCardDto().getCardNumber())
                .cardType(dto.getCardDto().getCardType())
                .account(account)
                .issuer(issuer)
                .expirationMonth(dto.getCardDto().getExpirationMonth())
                .expirationYear(dto.getCardDto().getExpirationYear())
                .active(true)
                .build();

        CardEntity saved = cardRepository.saveAndFlush(card);


        inMemoryRepository.saveCard(saved);

        log.info("Card created successfully: {}", saved.getCardNumber());

        return CardMapper.toDto(saved);
    }


    @Override
    public PagedResponseDto<CardResponseDto> searchCards(CardSearchDto searchDto) {
        log.debug("Searching cards with criteria: {}", searchDto);

        // ساخت Pageable از DTO
        Pageable pageable = buildPageable(searchDto);

        Page<CardEntity> cardPage = cardRepository.searchCards(
                searchDto.getNationalCode(),
                searchDto.getCardNumber(),
                searchDto.getIssuerCode(),
                searchDto.getCardType(),
                searchDto.getActive(),
                searchDto.getAccountNumber(),
                pageable
        );

        List<CardResponseDto> cardDtos = cardPage.getContent().stream()
                .map(CardResponseDto::fromEntity)
                .toList();

        return PagedResponseDto.<CardResponseDto>builder()
                .content(cardDtos)
                .pageNumber(cardPage.getNumber())
                .pageSize(cardPage.getSize())
                .totalElements(cardPage.getTotalElements())
                .totalPages(cardPage.getTotalPages())
                .last(cardPage.isLast())
                .build();
    }

private Pageable buildPageable(CardSearchDto searchDto) {
    if (searchDto.getPage() != null && searchDto.getSize() != null) {
        String sortBy = searchDto.getSortBy() != null ? searchDto.getSortBy() : "cardNumber";
        Sort.Direction direction = "DESC".equalsIgnoreCase(searchDto.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(searchDto.getPage(), searchDto.getSize(), Sort.by(direction, sortBy));
    }
    return Pageable.unpaged();
}

}

