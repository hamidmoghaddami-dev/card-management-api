package com.isc.cardManagement.repositoryTest;

import com.isc.cardManagement.entity.AccountEntity;
import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.entity.IssuerEntity;
import com.isc.cardManagement.entity.PersonEntity;
import com.isc.cardManagement.enums.AccountType;
import com.isc.cardManagement.enums.CardType;
import com.isc.cardManagement.repository.InMemoryRepository;
import com.isc.cardManagement.repository.jpa.AccountRepository;
import com.isc.cardManagement.repository.jpa.CardRepository;
import com.isc.cardManagement.repository.jpa.IssuerRepository;
import com.isc.cardManagement.repository.jpa.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InMemoryRepositoryTest {

    @Autowired
    private InMemoryRepository inMemoryRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private IssuerRepository issuerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    private static final String EXISTING_NATIONAL_CODE = "0063531425";

    @BeforeEach
    void setUp() {
        log.info("Setting up test...");

        // پاکسازی کامل (دیتابیس + کش)
        inMemoryRepository.clearAllIncludingDatabase();

        // لود مجدد داده‌ها
        inMemoryRepository.loadDataFromFile();

        // بررسی لود شدن
        Map<String, Integer> stats = inMemoryRepository.getStatistics();
        log.info("Repository statistics after load: {}", stats);

        assertTrue(
                stats.get("cards") > 0,
                "داده‌های اولیه باید لود شده باشند. Statistics: " + stats
        );
    }

    @AfterEach
    void tearDown() {
        log.info("Cleaning up after test...");
        inMemoryRepository.clearAllIncludingDatabase();
    }

    @Test
    @Order(1)
    @DisplayName("باید داده‌های اولیه را به درستی بارگذاری کند")
    void testLoadInitialData_success() {
        // When
        Set<CardEntity> cards = inMemoryRepository.getCardsByNationalCode(EXISTING_NATIONAL_CODE);

        // Then
        assertNotNull(cards, "مجموعه کارت‌ها نباید null باشد");
        assertFalse(cards.isEmpty(), "کارت‌ها باید مقدار داشته باشند");

        log.info("Found {} cards for national code {}", cards.size(), EXISTING_NATIONAL_CODE);
    }

    @Test
    @Order(2)
    @DisplayName("باید شخص را با کد ملی پیدا کند")
    void testFindPersonByNationalCode_success() {
        // When
        var person = inMemoryRepository.findPerson(EXISTING_NATIONAL_CODE);

        // Then
        assertTrue(person.isPresent(), "شخص باید پیدا شود");

        assertEquals(EXISTING_NATIONAL_CODE, person.get().getNationalCode());

        log.info("Found person: {} {}",
                person.get().getFirstName(),
                person.get().getLastName());
    }

    @Test
    @Order(3)
    @DisplayName("نباید اجازه ثبت کارت تکراری را بدهد")
    void testDuplicateCardInsertion_shouldThrowException() {
        // Given: گرفتن شخص و اطلاعات موجود
        PersonEntity person = personRepository.findByNationalCode(EXISTING_NATIONAL_CODE)
                .orElseThrow(() -> new AssertionError("Person not found"));

        AccountEntity account = person.getAccounts().iterator().next();
        CardEntity existingCard = account.getCards().iterator().next();

        // ساخت کارت تکراری (همان نوع و صادرکننده)
        CardEntity duplicateCard = new CardEntity();
        duplicateCard.setCardNumber("9999888877776666");
        duplicateCard.setCardType(existingCard.getCardType()); // همان نوع
        duplicateCard.setIssuer(existingCard.getIssuer()); // همان صادرکننده
        duplicateCard.setAccount(account);
        duplicateCard.setActive(true);
        duplicateCard.setExpirationMonth("10");
        duplicateCard.setExpirationYear("1407");

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inMemoryRepository.saveCard(duplicateCard),
                "باید استثنا برای کارت تکراری پرتاب شود"
        );

        // بررسی پیام خطا
        assertNotNull(exception.getMessage());
        assertTrue(
                exception.getMessage().contains("قبلاً یک کارت"),
                "پیام خطا باید در مورد کارت تکراری باشد. پیام واقعی: " + exception.getMessage()
        );

        log.info("Duplicate card correctly rejected: {}", exception.getMessage());
    }

    @Test
    @Order(4)
    @DisplayName("باید لیست خالی برای کد ملی نامعتبر برگرداند")
    void testFindCardsByInvalidNationalCode_shouldReturnEmptySet() {
        // When
        Set<CardEntity> cards = inMemoryRepository.getCardsByNationalCode("0000000000");

        // Then
        assertNotNull(cards, "مجموعه کارت‌ها نباید null باشد");
        assertTrue(cards.isEmpty(), "نباید کارتی برای کد ملی نامعتبر وجود داشته باشد");

        log.info("Correctly returned empty set for invalid national code");
    }

    @Test
    @Order(5)
    @DisplayName("باید صادرکننده را با کد صادرکننده پیدا کند")
    void testFindIssuerByCode_success() {
        // Given
        String issuerCode = "123456";

        // When
        var issuer = inMemoryRepository.findIssuer(issuerCode);

        // Then
        assertTrue(issuer.isPresent(), "صادرکننده باید پیدا شود");
        assertEquals(issuerCode, issuer.get().getIssuerCode());

        log.info("Found issuer: {}", issuer.get().getName());
    }

    @Test
    @Order(6)
    @DisplayName("باید حساب را با شماره حساب پیدا کند")
    void testFindAccountByNumber_success() {
        // Given
        String accountNumber = "1111111111";

        // When
        var account = inMemoryRepository.findAccount(accountNumber);

        // Then
        assertTrue(account.isPresent(), "حساب باید پیدا شود");
        assertEquals(accountNumber, account.get().getAccountNumber());

        log.info("Found account: {}", account.get().getAccountNumber());
    }

    @Test
    @Order(7)
    @DisplayName("باید کارت جدید معتبر را ذخیره کند")
    void testSaveNewValidCard_success() throws BadRequestException {
        // Given: ساخت موجودیت‌های مورد نیاز
        PersonEntity person = personRepository.findByNationalCode(EXISTING_NATIONAL_CODE)
                .orElseThrow(() -> new AssertionError("Person not found"));

        // ساخت صادرکننده جدید یا استفاده از موجود
        IssuerEntity issuer = issuerRepository.findByIssuerCode("654321")
                .orElseGet(() -> {
                    IssuerEntity newIssuer = new IssuerEntity();
                    newIssuer.setIssuerCode("999999");
                    newIssuer.setName("بانک تست");
                    return issuerRepository.save(newIssuer);
                });

        // ساخت حساب جدید
        AccountEntity account = new AccountEntity();
        account.setAccountNumber("9999999999");
        account.setAccountType(AccountType.SAVINGS);
        account.setOwner(person);
        account = accountRepository.saveAndFlush(account);

        // ساخت کارت جدید
        CardEntity newCard = new CardEntity();
        newCard.setCardNumber("5555444433332222");
        newCard.setCardType(CardType.CREDIT);
        newCard.setIssuer(issuer);
        newCard.setAccount(account);
        newCard.setActive(true);
        newCard.setExpirationMonth("06");
        newCard.setExpirationYear("1406");

        int initialCount = inMemoryRepository.getStatistics().get("cards");

        // When
        CardEntity savedCard = inMemoryRepository.saveCard(newCard);

        // Then
        assertNotNull(savedCard, "کارت ذخیره‌شده نباید null باشد");
        assertNotNull(savedCard.getId(), "ID کارت باید تنظیم شود");
        assertEquals(newCard.getCardNumber(), savedCard.getCardNumber());

        // بررسی افزایش تعداد در کش
        assertEquals(initialCount + 1, inMemoryRepository.getStatistics().get("cards"));

        // بررسی ذخیره در دیتابیس
        assertTrue(cardRepository.findById(savedCard.getId()).isPresent());

        log.info("New card saved successfully: {}", savedCard.getCardNumber());
    }

    @Test
    @Order(8)
    @DisplayName("باید همه داده‌ها را پاک کند")
    void testClearAll() {
        // Given: داده‌های موجود
        Map<String, Integer> initialStats = inMemoryRepository.getStatistics();
        assertTrue(initialStats.get("cards") > 0, "باید کارت‌هایی وجود داشته باشد");

        // When
        inMemoryRepository.clearAllIncludingDatabase();

        // Then
        Map<String, Integer> stats = inMemoryRepository.getStatistics();
        assertAll("All data should be cleared",
                () -> assertEquals(0, stats.get("persons")),
                () -> assertEquals(0, stats.get("issuers")),
                () -> assertEquals(0, stats.get("accounts")),
                () -> assertEquals(0, stats.get("cards")),
                () -> assertEquals(0, stats.get("nationalCodeEntries")),
                () -> assertEquals(0, personRepository.count()),
                () -> assertEquals(0, issuerRepository.count()),
                () -> assertEquals(0, accountRepository.count()),
                () -> assertEquals(0, cardRepository.count())
        );

        log.info("All data cleared successfully");
    }

    @Test
    @Order(9)
    @DisplayName("باید آمار صحیح را برگرداند")
    void testGetStatistics() {
        // When
        Map<String, Integer> stats = inMemoryRepository.getStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("persons"));
        assertTrue(stats.containsKey("issuers"));
        assertTrue(stats.containsKey("accounts"));
        assertTrue(stats.containsKey("cards"));
        assertTrue(stats.containsKey("nationalCodeEntries"));

        // بررسی همخوانی با دیتابیس
        assertEquals(personRepository.count(), stats.get("persons").longValue());
        assertEquals(issuerRepository.count(), stats.get("issuers").longValue());
        assertEquals(accountRepository.count(), stats.get("accounts").longValue());
        assertEquals(cardRepository.count(), stats.get("cards").longValue());

        log.info("Statistics correct: {}", stats);
    }

    @Test
    @Order(10)
    @DisplayName("باید کارت‌های یک شخص را به درستی برگرداند")
    void testGetCardsByNationalCode() {
        // Given
        String nationalCode = EXISTING_NATIONAL_CODE;

        // When
        Set<CardEntity> cards = inMemoryRepository.getCardsByNationalCode(nationalCode);

        // Then
        assertNotNull(cards);
        assertFalse(cards.isEmpty());

        // بررسی اینکه همه کارت‌ها متعلق به همان شخص هستند
        cards.forEach(card -> {
            assertNotNull(card.getAccount());
            assertNotNull(card.getAccount().getOwner());
            assertEquals(nationalCode, card.getAccount().getOwner().getNationalCode());
        });

        log.info("Found {} cards for national code {}", cards.size(), nationalCode);
    }

    @Test
    @Order(11)
    @DisplayName("باید مجموعه خالی برای کد ملی ناشناخته برگرداند")
    void testEmptyResultForUnknownNationalCode() {
        // When
        Set<CardEntity> cards = inMemoryRepository.getCardsByNationalCode("0000000000");

        // Then
        assertNotNull(cards);
        assertTrue(cards.isEmpty());

        log.info("Correctly returned empty set for unknown national code");
    }
}
