package com.isc.cardManagement.dto;

import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.enums.CardType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardResponseDto {

    private String cardNumber;
    private String expirationMonth;
    private String expirationYear;
    private boolean active;
    private CardType cardType;

    private IssuerDto issuer;
    private AccountDto account;
    private PersonDto person;

    public static CardResponseDto fromEntity(CardEntity card) {
        var issuer = card.getIssuer();
        var account = card.getAccount();
        var owner = account.getOwner();

        IssuerDto issuerDTO = new IssuerDto(issuer.getIssuerCode(), issuer.getName());
        AccountDto accountDTO = new AccountDto(account.getAccountNumber(), account.getAccountType());
        PersonDto personDto = new PersonDto(owner.getNationalCode(),
                owner.getFirstName(),
                owner.getLastName(),
                owner.getPhone(),
                owner.getAddress());

        return new CardResponseDto(
                card.getCardNumber(),
                card.getExpirationMonth(),
                card.getExpirationYear(),
                card.isActive(),
                card.getCardType(),
                issuerDTO,
                accountDTO,
                personDto
        );
    }
}
