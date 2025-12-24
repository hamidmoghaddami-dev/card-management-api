package com.isc.cardManagement.repository;

import com.isc.cardManagement.entity.AccountEntity;
import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.entity.IssuerEntity;
import com.isc.cardManagement.entity.PersonEntity;
import com.isc.cardManagement.enums.AccountType;
import com.isc.cardManagement.enums.CardType;
import com.isc.cardManagement.exception.BadRequestException;
import com.isc.cardManagement.exception.BusinessException;
import com.isc.cardManagement.repository.jpa.AccountRepository;
import com.isc.cardManagement.repository.jpa.CardRepository;
import com.isc.cardManagement.repository.jpa.IssuerRepository;
import com.isc.cardManagement.repository.jpa.PersonRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
//@DependsOn("entityManagerFactory")
public class InMemoryRepository {


    private final Map<String, PersonEntity> personMap = new ConcurrentHashMap<>();

    private final Map<String, IssuerEntity> issuerMap = new ConcurrentHashMap<>();

    private final Map<String, AccountEntity> accountMap = new ConcurrentHashMap<>();

    private final Map<String, CardEntity> cardMap = new ConcurrentHashMap<>();

    private final Map<String, Set<CardEntity>> nationalCodeCardsMap = new ConcurrentHashMap<>();
    private final Map<String, CardEntity> uniqueCardConstraintMap = new ConcurrentHashMap<>();

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final IssuerRepository issuerRepository;

    @Value("${app.data.file-path:data/initial-data.txt}")
    private String dataFilePath;


    @Transactional
    public void clearAllIncludingDatabase() {

        log.info("Clearing all data (cache + database)...");

        cardRepository.deleteAll();
        accountRepository.deleteAll();
        issuerRepository.deleteAll();
        personRepository.deleteAll();

        clearAll();

        log.info("All data cleared (cache + database)");
    }


    public Optional<PersonEntity> findPerson(String nationalCode) {
        PersonEntity cached = personMap.get(nationalCode);

        if (cached != null) {
            log.debug("Person found in cache: {}", nationalCode);
            return Optional.of(cached);
        }

        log.debug("Person not in cache, fetching from DB: {}", nationalCode);
        Optional<PersonEntity> fromDb = personRepository.findByNationalCode(nationalCode);

        fromDb.ifPresent(person -> {
            personMap.put(nationalCode, person);
            log.debug("Person cached: {}", nationalCode);
        });

        return fromDb;
    }


    private void validateCard(CardEntity card) {

        Objects.requireNonNull(card, "کارت نمی تواند خالی باشد");
        Objects.requireNonNull(card.getAccount(), "حساب کارت نمی تواند خالی باشد");
        Objects.requireNonNull(card.getAccount().getOwner(), "مالک حساب نمی تواند خالی باشد");
        Objects.requireNonNull(card.getIssuer(), "صادرکننده نمی تواند خالی باشد");
    }

    @PostConstruct
    @Transactional
    public void init() {
        log.info("Initializing InMemoryRepository...");
        try {
            loadDataFromFile();
            log.info("InMemoryRepository initialized successfully");
            printStatistics();
        } catch (Exception e) {
            log.error("Failed to initialize InMemoryRepository", e);
        }
    }


    public void loadDataFromFile() {
        try {
            ClassPathResource resource = new ClassPathResource(dataFilePath);
            List<String> lines = Files.readAllLines(
                    Paths.get(resource.getURI()),
                    StandardCharsets.UTF_8
            );

            for (String line : lines) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.toLowerCase().startsWith("person=")) {
                    processPerson(line.substring(7));
                } else if (line.toLowerCase().startsWith("issuer=")) {
                    processIssuer(line.substring(7));
                } else if (line.toLowerCase().startsWith("account=")) {
                    processAccount(line.substring(8));
                } else if (line.toLowerCase().startsWith("card=")) {
                    processCard(line.substring(5));
                }
            }

        } catch (Exception e) {
            log.error("Failed to load data from file", e);
            throw new BusinessException("Failed to load initial data", e);
        }
    }

    private void processPerson(String data) {
        try {
            String[] tokens = data.split(",");
            if (tokens.length < 5) {
                log.warn("Invalid person format: {}", data);
                return;
            }

            String firstName = tokens[0].trim();
            String lastName = tokens[1].trim();
            String nationalCode = tokens[2].trim();
            String phone = tokens[3].trim();
            String address = tokens[4].trim();

            PersonEntity person = personRepository
                    .findByNationalCode(nationalCode)
                    .orElseGet(() -> {
                        PersonEntity newPerson = new PersonEntity();
                        newPerson.setFirstName(firstName);
                        newPerson.setLastName(lastName);
                        newPerson.setNationalCode(nationalCode);
                        newPerson.setPhone(phone);
                        newPerson.setAddress(address);
                        return personRepository.saveAndFlush(newPerson);
                    });

            personMap.put(nationalCode, person);
            nationalCodeCardsMap.putIfAbsent(nationalCode, ConcurrentHashMap.newKeySet());

            log.debug("Person saved: {} {} ({})", firstName, lastName, nationalCode);

        } catch (Exception e) {
            log.error("Error processing person: {}", data, e);
        }
    }

    private void processIssuer(String data) {
        try {
            String[] tokens = data.split(",", 2);
            if (tokens.length < 2) {
                log.warn("Invalid issuer format: {}", data);
                return;
            }

            String issuerCode = tokens[0].trim();
            String issuerName = tokens[1].trim();

            IssuerEntity issuer = issuerRepository
                    .findByIssuerCode(issuerCode)
                    .orElseGet(() -> {
                        IssuerEntity newIssuer = new IssuerEntity();
                        newIssuer.setIssuerCode(issuerCode);
                        newIssuer.setName(issuerName);
                        return issuerRepository.saveAndFlush(newIssuer);
                    });

            issuerMap.put(issuerCode, issuer);

            log.debug("Issuer saved: {} ({})", issuerName, issuerCode);

        } catch (Exception e) {
            log.error("Error processing issuer: {}", data, e);
        }
    }


    private void processAccount(String data) {

        try {

            String[] tokens = data.split(",");
            if (tokens.length < 3) {
                log.warn("Invalid account format: {}", data);
                return;
            }

            String accountNumber = tokens[0].trim();
            String accountTypeStr = tokens[1].trim().toUpperCase();
            String nationalCode = tokens[2].trim();

            log.debug("Processing account: {} for person: {}", accountNumber, nationalCode);

            AccountType accountType;

            try {
                accountType = AccountType.valueOf(accountTypeStr);
            } catch (IllegalArgumentException e) {
                log.error("Invalid account type: {}", accountTypeStr);
                return;
            }

            if (!personMap.containsKey(nationalCode)) {
                log.error("Person not found in cache: {}", nationalCode);
                return;
            }

            Optional<AccountEntity> existingAccount = accountRepository.findByAccountNumber(accountNumber);
            if (existingAccount.isPresent()) {
                accountMap.put(accountNumber, existingAccount.get());
                log.debug("Account already exists in DB: {}", accountNumber);
                return;
            }

            PersonEntity managedPerson = personRepository.findByNationalCode(nationalCode)
                    .orElseThrow(() -> new RuntimeException("Person not found in DB: " + nationalCode));

            AccountEntity newAccount = AccountEntity.builder()
                    .accountNumber(accountNumber)
                    .accountType(accountType)
                    .owner(managedPerson)  // Managed Entity
                    .build();

            AccountEntity savedAccount = accountRepository.saveAndFlush(newAccount);
            accountMap.put(accountNumber, savedAccount);

            log.info("Account created: {} for person: {}", accountNumber, nationalCode);

        } catch (Exception e) {
            log.error("Error processing account: {}", data, e);
        }
    }


    private String buildUniqueCardKey(String nationalCode, String cardType, String issuerCode) {
        return String.format("%s_%s_%s", nationalCode, cardType, issuerCode);
    }


    private void printStatistics() {

        log.info(" ═══════════════════════════════════════");
        log.info(" Final Cache Statistics:");
        log.info(" ─────────────────────────────────────");
        log.info(" Persons in cache     : {}", personMap.size());
        log.info(" Issuers in cache     : {}", issuerMap.size());
        log.info(" Accounts in cache    : {}", accountMap.size());
        log.info(" Cards in cache       : {}", cardMap.size());
        log.info(" Unique card constraints: {}", uniqueCardConstraintMap.size());
        log.info(" Cards by national code: {}", nationalCodeCardsMap.size());

        nationalCodeCardsMap.forEach((nationalCode, cards) -> {
            log.info("   {} has {} card(s)", nationalCode, cards.size());
            cards.forEach(card -> log.info("     - {} {} from {}",
                    card.getCardType(),
                    card.getCardNumber(),
                    card.getIssuer().getIssuerCode()));
        });

        log.info("═══════════════════════════════════════");
    }

    @Transactional
    public CardEntity saveCard(CardEntity card) throws BadRequestException {
        validateCard(card);
        String nationalCode = card.getAccount().getOwner().getNationalCode();

        String uniqueKey = buildUniqueKey(nationalCode, card.getCardType(),
                card.getIssuer().getIssuerCode());

        if (uniqueCardConstraintMap.containsKey(uniqueKey)) {
            throw new BadRequestException(
                    String.format("کارت تکراری -> کد ملی: %s - %s (%s)",
                            nationalCode,
                            card.getCardType().name().equals("DEBIT") ? "نقدی" : "اعتباری",
                            card.getIssuer().getName())
            );
        }

        CardEntity saved = cardRepository.saveAndFlush(card);

        syncCardToCache(saved, nationalCode, uniqueKey);

        log.info(" Card synced: {} for person {}", saved.getCardNumber(), nationalCode);
        return saved;
    }

    @Transactional(readOnly = true)
    public Set<CardEntity> getCardsByNationalCode(String nationalCode) {
        Set<CardEntity> cachedCards = nationalCodeCardsMap.get(nationalCode);

        if (cachedCards != null && !cachedCards.isEmpty()) {
            log.debug("Cache hit: {} card(s) for {}", cachedCards.size(), nationalCode);
            return new HashSet<>(cachedCards);
        }

        PersonEntity person = personRepository.findByNationalCode(nationalCode).orElse(null);
        if (person == null) {
            return Collections.emptySet();
        }

        List<AccountEntity> accounts = accountRepository.findAllByOwner(person);
        Set<CardEntity> dbCards = accounts.stream()
                .flatMap(account -> cardRepository.findAllByAccount(account).stream())
                .collect(Collectors.toSet());

        dbCards.forEach(card -> {
            String uniqueKey = buildUniqueKey(nationalCode, card.getCardType(),
                    card.getIssuer().getIssuerCode());
            syncCardToCache(card, nationalCode, uniqueKey);
        });

        log.info("Synced {} card(s) from DB to cache for {}", dbCards.size(), nationalCode);
        return dbCards;
    }

    private void processCard(String data) {

        try {
            String[] tokens = data.split(",");
            if (tokens.length < 7) {
                log.warn("Invalid card format: {}", data);
                return;
            }

            String cardNumber = tokens[0].trim();
            String cardTypeStr = tokens[1].trim().toUpperCase();
            boolean active = Boolean.parseBoolean(tokens[2].trim());
            String expirationMonth = tokens[3].trim();
            String expirationYear = tokens[4].trim();
            String issuerCode = tokens[5].trim();
            String accountNumber = tokens[6].trim();

            log.debug("Processing card: {} for account: {}", cardNumber, accountNumber);

            CardType cardType;
            try {
                cardType = CardType.valueOf(cardTypeStr);
            } catch (IllegalArgumentException e) {
                log.error("Invalid card type: {}", cardTypeStr);
                return;
            }

            if (!accountMap.containsKey(accountNumber)) {
                log.error("Account not found in cache: {}", accountNumber);
                return;
            }
            if (!issuerMap.containsKey(issuerCode)) {
                log.error("Issuer not found in cache: {}", issuerCode);
                return;
            }

            AccountEntity cachedAccount = accountMap.get(accountNumber);
            String nationalCode = cachedAccount.getOwner().getNationalCode();
            String uniqueKey = buildUniqueKey(nationalCode, cardType, issuerCode);

            if (uniqueCardConstraintMap.containsKey(uniqueKey)) {
                log.warn("Duplicate card constraint violated: {}", uniqueKey);
                return;
            }

            Optional<CardEntity> existingCard = cardRepository.findByCardNumber(cardNumber);
            if (existingCard.isPresent()) {
                syncCardToCache(existingCard.get(), nationalCode, uniqueKey);
                log.debug("Card already exists in DB: {}", cardNumber);
                return;
            }

            AccountEntity managedAccount = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException("Account not found in DB: " + accountNumber));

            IssuerEntity managedIssuer = issuerRepository.findByIssuerCode(issuerCode)
                    .orElseThrow(() -> new RuntimeException("Issuer not found in DB: " + issuerCode));

            CardEntity newCard = CardEntity.builder()
                    .cardNumber(cardNumber)
                    .cardType(cardType)
                    .active(active)
                    .expirationMonth(expirationMonth)
                    .expirationYear(expirationYear)
                    .account(managedAccount)  // Managed
                    .issuer(managedIssuer)    // Managed
                    .build();

            CardEntity savedCard = cardRepository.saveAndFlush(newCard);
            syncCardToCache(savedCard, nationalCode, uniqueKey);

            log.info("Card created: {} for account: {}", cardNumber, accountNumber);

        } catch (Exception e) {
            log.error("Error processing card: {}", data, e);
        }
    }


    private void syncCardToCache(CardEntity card, String nationalCode, String uniqueKey) {
        cardMap.put(card.getCardNumber(), card);
        nationalCodeCardsMap.computeIfAbsent(nationalCode, k -> ConcurrentHashMap.newKeySet()).add(card);
        uniqueCardConstraintMap.put(uniqueKey, card);
    }

    private String buildUniqueKey(String nationalCode, CardType cardType, String issuerCode) {
        return String.format("%s_%s_%s", nationalCode, cardType, issuerCode);
    }


    private IssuerEntity parseIssuer(String data) {
        String[] tokens = data.split(",");
        IssuerEntity issuer = new IssuerEntity();
        issuer.setIssuerCode(tokens[0].trim());
        issuer.setName(tokens[1].trim());
        return issuer;
    }


    public Optional<IssuerEntity> findIssuer(String issuerCode) {
        return Optional.ofNullable(issuerMap.get(issuerCode));
    }

    public Optional<AccountEntity> findAccount(String accountNumber) {
        return Optional.ofNullable(accountMap.get(accountNumber));
    }


    public Map<String, Set<CardEntity>> getAll() {
        return Collections.unmodifiableMap(nationalCodeCardsMap);
    }

    public void clearAll() {
        nationalCodeCardsMap.clear();
        personMap.clear();
        issuerMap.clear();
        accountMap.clear();
        cardMap.clear();
        log.info("In-memory repository cleared");
    }

    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("persons", personMap.size());
        stats.put("issuers", issuerMap.size());
        stats.put("accounts", accountMap.size());
        stats.put("cards", cardMap.size());
        stats.put("nationalCodeEntries", nationalCodeCardsMap.size());
        return stats;
    }

}

