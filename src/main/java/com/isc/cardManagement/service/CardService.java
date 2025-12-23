package com.isc.cardManagement.service;

import com.isc.cardManagement.dto.CardDto;
import com.isc.cardManagement.dto.CardResponseDto;
import com.isc.cardManagement.dto.CreateCardRequestDto;
import com.isc.cardManagement.exception.BadRequestException;

import java.util.List;

public interface CardService {

        List<CardResponseDto> getCardsByNationalCode(String nationalCode);
        CardDto createCard(CreateCardRequestDto dto) throws BadRequestException;


}
