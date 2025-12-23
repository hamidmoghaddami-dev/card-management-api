package com.isc.cardManagement.service;

import com.isc.cardManagement.dto.CardDto;
import com.isc.cardManagement.dto.CardResponseDto;
import com.isc.cardManagement.dto.CreateCardRequestDto;
import org.apache.coyote.BadRequestException;

import java.util.List;

public interface CardService {

        List<CardResponseDto> getCardsByNationalCode(String nationalCode);
        CardDto createCard(CreateCardRequestDto dto) throws BadRequestException;


}
