package com.isc.cardManagement.mapper;

import com.isc.cardManagement.dto.CardDto;
import com.isc.cardManagement.entity.CardEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CardMapper {

    public static CardEntity toEntity(CardDto dto) {
        if (dto == null) return null;

        CardEntity entity = new CardEntity();
        entity.setCardNumber(dto.getCardNumber());
        entity.setExpirationMonth(dto.getExpirationMonth());
        entity.setExpirationYear(dto.getExpirationYear());
        entity.setActive(dto.isActive());
        entity.setCardType(dto.getCardType());
        return entity;
    }

    public static CardDto toDto(CardEntity entity) {
        if (entity == null) return null;

        return CardDto.builder()
                .cardNumber(entity.getCardNumber())
                .expirationMonth(entity.getExpirationMonth())
                .expirationYear(entity.getExpirationYear())
                .active(entity.isActive())
                .cardType(entity.getCardType())
                .accountNumber(entity.getAccount().getAccountNumber())
                .issuerCode(entity.getIssuer().getIssuerCode())
                .build();
    }

    public static List<CardDto> toDtoList(List<CardEntity> entities) {
        if (entities == null) return Collections.emptyList();

        return entities.stream()
                .map(CardMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<CardEntity> toEntityList(List<CardDto> dtos) {

        if (dtos == null) {
            return Collections.emptyList();
        }

        return dtos.stream()
                .map(CardMapper::toEntity)
                .collect(Collectors.toList());
    }
}

