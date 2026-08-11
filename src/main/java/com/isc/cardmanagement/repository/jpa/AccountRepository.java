package com.isc.cardmanagement.repository.jpa;

import com.isc.cardmanagement.entity.AccountEntity;
import com.isc.cardmanagement.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);
    List<AccountEntity> findAllByOwner(PersonEntity person);

}
