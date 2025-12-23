package com.isc.cardManagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tbl_issuer")
@Data
@NoArgsConstructor
public class IssuerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issuer_id")
    private Long id;

    @NotBlank
    @Size(min = 6, max = 6)
    @Pattern(regexp = "\\d+")
    @Column(name = "issuer_code", nullable = false, unique = true, length = 6)
    private String issuerCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "issuer_name", nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "issuer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CardEntity> cards = new HashSet<>();


    public void addCard(CardEntity card) {
        cards.add(card);
        card.setIssuer(this);
    }

    public void removeCard(CardEntity card) {
        cards.remove(card);
        card.setIssuer(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IssuerEntity)) return false;
        IssuerEntity that = (IssuerEntity) o;
        return issuerCode != null && issuerCode.equals(that.issuerCode);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("IssuerEntity[id=%d, code=%s, name=%s]",
                id, issuerCode, name);
    }

}

