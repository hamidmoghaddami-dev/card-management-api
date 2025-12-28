package com.isc.cardManagement.entity;

import com.isc.cardManagement.enums.CardType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;


@Entity
@Table(name = "tbl_card",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_person_cardtype_issuer",
                        columnNames = {"account_id", "card_type", "issuer_id"}
                )
        })
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long id;

    @NotBlank(message = "card.number.can.not.be.null")
    @Size(min = 16, max = 16)
    @Pattern(regexp = "\\d+")
    @Column(name = "card_number", length = 16, unique = true, nullable = false)
    private String cardNumber;

    @NotNull(message = "expiration.month.can.not.be.null")
    @Size(min = 2, max = 2)
    @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "ماه باید بین 01 تا 12 باشد")
    @Column(name = "expiration_month", nullable = false)
    private String expirationMonth;

    @NotNull(message = "expiration.year.can.not.be.null")
    @Column(name = "expiration_year", nullable = false)
    private String expirationYear;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @NotNull(message = "card.type.can.not.be.null")
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @NotNull(message = "issuer.can.not.be.null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "issuer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_card_issuer"))
    private IssuerEntity issuer;

    @NotNull(message = "account.can.not.be.null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_card_account"))
    private AccountEntity account;


    public boolean isExpired() {
        try {
            int year = Integer.parseInt(expirationYear);
            int month = Integer.parseInt(expirationMonth);

            LocalDate expirationDate = LocalDate.of(year, month, 1)
                    .plusMonths(1)
                    .minusDays(1);

            return LocalDate.now().isAfter(expirationDate);
        } catch (Exception e) {
            return true;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardEntity that)) return false;
        return cardNumber != null && cardNumber.equals(that.cardNumber);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("CardEntity[id=%d, cardNumber=%s, type=%s, active=%s]",
                id, cardNumber, cardType, active);
    }

}


