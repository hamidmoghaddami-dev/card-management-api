package com.isc.cardManagement.repository.jpa;

import com.isc.cardManagement.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository  extends JpaRepository<PersonEntity, Long> {

    Optional<PersonEntity> findByNationalCode(String nationalCode);
}
