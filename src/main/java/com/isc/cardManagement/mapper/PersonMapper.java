package com.isc.cardManagement.mapper;

import com.isc.cardManagement.dto.PersonDto;
import com.isc.cardManagement.entity.PersonEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Data
public class PersonMapper {

    // DTO -> Entity
    public static PersonEntity toEntity(PersonDto dto) {
        if (dto == null) return null;

        PersonEntity entity = new PersonEntity();
        entity.setNationalCode(dto.getNationalCode());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        // توجه: حساب‌ها رو اینجا نمیشه ست کرد چون در DTO نیستند
        return entity;
    }

    // Entity -> DTO
    public static PersonDto toDto(PersonEntity entity) {
        if (entity == null) return null;

        return PersonDto.builder()
                .nationalCode(entity.getNationalCode())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .build();
    }


    // تبدیل لیست Entity به لیست DTO
    public static List<PersonDto> toDtoList(List<PersonEntity> entities) {
        if (entities == null) return Collections.emptyList();

        return entities.stream()
                .map(PersonMapper::toDto)
                .collect(Collectors.toList());
    }

    // تبدیل لیست DTO به لیست Entity
    public static List<PersonEntity> toEntityList(List<PersonDto> dtos) {
        if (dtos == null) return Collections.emptyList();

        return dtos.stream()
                .map(PersonMapper::toEntity)
                .collect(Collectors.toList());
    }
}

