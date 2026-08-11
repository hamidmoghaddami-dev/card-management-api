package com.isc.cardmanagement.service;

import com.isc.cardmanagement.dto.*;
import com.isc.cardmanagement.exception.BadRequestException;

import java.util.List;

public interface CardService {

    List<CardResponseDto> getCardsByNationalCode(String nationalCode);

    CardDto createCard(CardDto dto) throws BadRequestException;


    PagedResponseDto<CardResponseDto> searchCards(CardSearchDto searchDto);
}
