package com.isc.cardmanagement.service;

import com.isc.cardmanagement.dto.CardDto;
import com.isc.cardmanagement.dto.CardResponseDto;
import com.isc.cardmanagement.dto.CardSearchDto;
import com.isc.cardmanagement.dto.PagedResponseDto;

import java.util.List;

public interface CardService {

    List<CardResponseDto> getCardsByNationalCode(String nationalCode);

    CardDto createCard(CardDto dto);

    PagedResponseDto<CardResponseDto> searchCards(CardSearchDto searchDto);
}
