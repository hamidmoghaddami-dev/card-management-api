package com.isc.cardManagement.repository.jpa;

import com.isc.cardManagement.entity.IssuerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IssuerRepository extends JpaRepository<IssuerEntity, Long> {

    Optional<IssuerEntity> findByIssuerCode(String issuerCode);
}
