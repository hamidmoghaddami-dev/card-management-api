package com.isc.cardManagement.entity;


import com.isc.cardManagement.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tbl_account")
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @NotBlank(message = "account.number.can.not.be.null")
    @Size(min = 10, max = 10)
    @Pattern(regexp = "\\d+")
    @Column(name = "account_number", unique = true, nullable = false, length = 10)
    private String accountNumber;

    @NotNull(message = "account.type.can.not.be.null")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @NotNull(message = "owner.can.not.be.null")
    @ManyToOne
    @JoinColumn(name = "person_id")
    private PersonEntity owner;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CardEntity> cards = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountEntity that)) return false;
        return accountNumber != null && accountNumber.equals(that.accountNumber);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("AccountEntity[id=%d, accountNumber=%s, type=%s]",
                id, accountNumber, accountType);
    }

}


