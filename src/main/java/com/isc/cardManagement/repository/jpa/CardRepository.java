package com.isc.cardManagement.repository.jpa;

import com.isc.cardManagement.entity.AccountEntity;
import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.enums.CardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Long> {

    Optional<CardEntity> findByCardNumber(String cardNumber);

    List<CardEntity> findAllByAccount(AccountEntity accountEntity);

    List<CardEntity> findByAccount_Owner_NationalCode(String nationalCode);

    @Query("""
        SELECT c FROM CardEntity c
        WHERE c.account.owner.nationalCode = :nationalCode
        AND c.cardType = :cardType
        AND c.issuer.issuerCode = :issuerCode
    """)
    Optional<CardEntity> findByOwnerAndTypeAndIssuer(
            @Param("nationalCode") String nationalCode,
            @Param("cardType") CardType cardType,
            @Param("issuerCode") String issuerCode
    );

    @Query("""
        SELECT COUNT(c) FROM CardEntity c
        WHERE c.account.owner.nationalCode = :nationalCode
        AND c.cardType = :cardType
        AND c.issuer.issuerCode = :issuerCode
    """)
    long countByOwnerAndTypeAndIssuer(
            @Param("nationalCode") String nationalCode,
            @Param("cardType") CardType cardType,
            @Param("issuerCode") String issuerCode
    );

    @Query("""
        SELECT c FROM CardEntity c
        WHERE c.account.id = :accountId
        AND c.cardType = :cardType
        AND c.issuer.issuerCode = :issuerCode
    """)
    Optional<CardEntity> findByAccountAndTypeAndIssuer(
            @Param("accountId") Long accountId,
            @Param("cardType") CardType cardType,
            @Param("issuerCode") String issuerCode
    );


    @Query("""
            SELECT DISTINCT c FROM CardEntity c
            LEFT JOIN FETCH c.account a
            LEFT JOIN FETCH a.owner p
            LEFT JOIN FETCH c.issuer i
            WHERE (:nationalCode IS NULL OR p.nationalCode = :nationalCode)
              AND (:cardNumber IS NULL OR c.cardNumber = :cardNumber)
              AND (:issuerCode IS NULL OR i.issuerCode = :issuerCode)
              AND (:cardType IS NULL OR c.cardType = :cardType)
              AND (:active IS NULL OR c.active = :active)
              AND (:accountNumber IS NULL OR a.accountNumber = :accountNumber)
            """)
    Page<CardEntity> searchCards(
            @Param("nationalCode") String nationalCode,
            @Param("cardNumber") String cardNumber,
            @Param("issuerCode") String issuerCode,
            @Param("cardType") CardType cardType,
            @Param("active") Boolean active,
            @Param("accountNumber") String accountNumber,
            Pageable pageable
    );

}
