package com.isc.cardManagement.mapper;

import com.isc.cardManagement.dto.IssuerDto;
import com.isc.cardManagement.entity.IssuerEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class IssuerMapper {

    public static IssuerEntity toEntity(IssuerDto dto) {
        if (dto == null) return null;

        IssuerEntity entity = new IssuerEntity();
        entity.setIssuerCode(dto.getIssuerCode());
        entity.setName(dto.getName());
        return entity;
    }

    public static IssuerDto toDto(IssuerEntity entity) {
        if (entity == null) return null;

        return IssuerDto.builder()
                .issuerCode(entity.getIssuerCode())
                .name(entity.getName())
                .build();
    }

    public static List<IssuerDto> toDtoList(List<IssuerEntity> entities) {
        if (entities == null) return Collections.emptyList();

        return entities.stream()
                .map(IssuerMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<IssuerEntity> toEntityList(List<IssuerDto> dtos) {
        if (dtos == null) return Collections.emptyList();

        return dtos.stream()
                .map(IssuerMapper::toEntity)
                .collect(Collectors.toList());
    }
}

