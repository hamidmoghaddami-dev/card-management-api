package com.isc.cardManagement.service;

import com.isc.cardManagement.dto.*;
import com.isc.cardManagement.exception.BadRequestException;

import java.util.List;

public interface CardService {

    List<CardResponseDto> getCardsByNationalCode(String nationalCode);

    CardDto createCard(CardDto dto) throws BadRequestException;


    PagedResponseDto<CardResponseDto> searchCards(CardSearchDto searchDto);
}
