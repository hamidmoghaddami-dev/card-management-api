package com.isc.cardManagement.mapper;

import com.isc.cardManagement.dto.AccountDto;
import com.isc.cardManagement.entity.AccountEntity;
import com.isc.cardManagement.entity.PersonEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AccountMapper {

    public static AccountEntity toEntity(AccountDto dto, PersonEntity owner) {
        if (dto == null) return null;

        AccountEntity entity = new AccountEntity();
        entity.setAccountNumber(dto.getAccountNumber());
        entity.setAccountType(dto.getAccountType());
        entity.setOwner(owner);
        return entity;
    }

    public static AccountDto toDto(AccountEntity entity) {
        if (entity == null) return null;

        return AccountDto.builder()
                .accountNumber(entity.getAccountNumber())
                .accountType(entity.getAccountType())
                .build();
    }

    public static List<AccountDto> toDtoList(List<AccountEntity> entities) {
        if (entities == null) return Collections.emptyList();

        return entities.stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<AccountEntity> toEntityList(List<AccountDto> dtos, PersonEntity owner) {
        if (dtos == null) return Collections.emptyList();

        return dtos.stream()
                .map(dto -> AccountMapper.toEntity(dto, owner))
                .collect(Collectors.toList());
    }
}

