package com.isc.cardManagement.repositoryTest;

import com.isc.cardManagement.entity.AccountEntity;
import com.isc.cardManagement.entity.CardEntity;
import com.isc.cardManagement.entity.IssuerEntity;
import com.isc.cardManagement.entity.PersonEntity;
import com.isc.cardManagement.enums.AccountType;
import com.isc.cardManagement.enums.CardType;
import com.isc.cardManagement.exception.BadRequestException;
import com.isc.cardManagement.repository.InMemoryRepository;
import com.isc.cardManagement.repository.jpa.AccountRepository;
import com.isc.cardManagement.repository.jpa.CardRepository;
import com.isc.cardManagement.repository.jpa.IssuerRepository;
import com.isc.cardManagement.repository.jpa.PersonRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;


@DataJpaTest
@Import(InMemoryRepository.class)
@TestPropertySource(properties = {
        "app.data.file-path=test-data-empty.txt",  // فایل خالی
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("InMemoryRepository unit Tests")
class InMemoryRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

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


    private IssuerEntity testIssuer;
    private AccountEntity testAccount;

    @BeforeEach
    void setUp() {
        // پاک کردن Cache قبل از هر تست
        inMemoryRepository.clearAll();

        // ایجاد دیتای تستی در دیتابیس
        PersonEntity testPerson = PersonEntity.builder()
                .nationalCode("1234567890")
                .firstName("علی")
                .lastName("احمدی")
                .phone("09121234567")
                .address("تهران، خیابان ولیعصر")
                .build();
        testPerson = entityManager.persistAndFlush(testPerson);

        testIssuer = IssuerEntity.builder()
                .issuerCode("627353")
                .name("بانک تجارت")
                .build();
        testIssuer = entityManager.persistAndFlush(testIssuer);

        testAccount = AccountEntity.builder()
                .accountNumber("1234567890")
                .accountType(AccountType.SAVINGS)
                .owner(testPerson)
                .build();
        testAccount = entityManager.persistAndFlush(testAccount);

        entityManager.clear();
    }

    // ========== Person Tests ==========

    @Test
    @Order(1)
    @DisplayName("باید شخص را از دیتابیس بخواند و در Cache ذخیره کند")
    void shouldFindPersonFromDatabaseAndCacheIt() {
        // When - بار اول: از DB می‌خواند
        Optional<PersonEntity> found = inMemoryRepository.findPerson("1234567890");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNationalCode()).isEqualTo("1234567890");
        assertThat(found.get().getFirstName()).isEqualTo("علی");

        // Verify که در Cache ذخیره شده
        Optional<PersonEntity> cachedPerson = inMemoryRepository.findPerson("1234567890");
        assertThat(cachedPerson).isPresent();
        assertThat(cachedPerson.get()).isEqualTo(found.get());
    }

    @Test
    @Order(2)
    @DisplayName("باید Optional.empty برگرداند برای کدملی نامعتبر")
    void shouldReturnEmptyForInvalidNationalCode() {
        // When
        Optional<PersonEntity> found = inMemoryRepository.findPerson("9999999999");

        // Then
        assertThat(found).isEmpty();
    }

    // ========== Issuer Tests ==========

    @Test
    @Order(3)
    @DisplayName("باید صادرکننده را در Cache ذخیره کند")
    void shouldCacheIssuer() {
        //when
        issuerRepository.findByIssuerCode("627353");


        Optional<IssuerEntity> found = inMemoryRepository.findIssuer("627353");

        assertThat(found).isEmpty();
    }

    // ========== Account Tests ==========

    @Test
    @Order(4)
    @DisplayName("باید حساب را از دیتابیس پیدا کند")
    void shouldFindAccountFromDatabase() {

        // When
        Optional<AccountEntity> found = inMemoryRepository.findAccount("1234567890");

        // Then
        assertThat(found).isEmpty();

        Optional<AccountEntity> dbAccount = accountRepository.findByAccountNumber("1234567890");
        assertThat(dbAccount).isPresent();
    }

    // ========== Card Save Tests ==========

    @Test
    @Order(5)
    @DisplayName("باید کارت جدید را ذخیره و سینک کند")
    void shouldSaveNewCardAndSync() throws BadRequestException {

        // Given
        IssuerEntity newIssuer = IssuerEntity.builder()
                .issuerCode("627359")
                .name("بانک منحل آینده")
                .build();

        newIssuer = entityManager.persistAndFlush(newIssuer);

        CardEntity newCard = CardEntity.builder()
                .cardNumber("6273531234567890")
                .cardType(CardType.CREDIT)
                .active(true)
                .expirationMonth("12")
                .expirationYear("1405")
                .account(testAccount)
                .issuer(newIssuer)
                .build();

        // When
        CardEntity saved = inMemoryRepository.saveCard(newCard);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        Optional<CardEntity> dbCard = cardRepository.findByCardNumber("6273531234567890");
        assertThat(dbCard).isPresent();
        assertThat(dbCard.get().getCardNumber()).isEqualTo("6273531234567890");

        Set<CardEntity> cards = inMemoryRepository.getCardsByNationalCode("1234567890");
        assertThat(cards).hasSize(1);
        assertThat(cards.iterator().next().getCardNumber()).isEqualTo("6273531234567890");
    }

    @Test
    @Order(6)
    @DisplayName("باید خطای تکراری بودن کارت را پرتاب کند")
    void shouldThrowExceptionForDuplicateCard() {

        PersonEntity newPerson = PersonEntity.builder()
                .nationalCode("9876543210")
                .firstName("محمد")
                .lastName("محمدی")
                .phone("09127654321")
                .address("تهران - خیابان آزادی")
                .build();
        personRepository.saveAndFlush(newPerson);

        IssuerEntity newIssuer = IssuerEntity.builder()
                .issuerCode("627359")
                .name("بانک صادرات")
                .build();
        issuerRepository.saveAndFlush(newIssuer);

        AccountEntity newAccount = AccountEntity.builder()
                .accountNumber("9876543210")
                .accountType(AccountType.SAVINGS)
                .owner(newPerson)
                .build();
        accountRepository.saveAndFlush(newAccount);

        CardEntity firstCard = CardEntity.builder()
                .cardNumber("6273599999999999")
                .cardType(CardType.CREDIT)
                .active(true)
                .expirationMonth("06")
                .expirationYear("1406")
                .account(newAccount)
                .issuer(newIssuer)
                .build();

        try {
            inMemoryRepository.saveCard(firstCard);
            entityManager.flush();
            entityManager.clear();
        } catch (Exception e) {
            fail("First card should be saved successfully", e);
        }

        CardEntity duplicateCard = CardEntity.builder()
                .cardNumber("6273598888888888") // شماره متفاوت
                .cardType(CardType.CREDIT)      // همان نوع
                .active(true)
                .expirationMonth("12")
                .expirationYear("1407")
                .account(newAccount)           // همان حساب
                .issuer(newIssuer)             // همان صادرکننده
                .build();

        assertThatThrownBy(() -> inMemoryRepository.saveCard(duplicateCard))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("کارت تکراری")
                .hasMessageContaining("9876543210");
    }


    // ========== Get Cards Tests ==========

    @Test
    @Order(7)
    @DisplayName("باید کارت‌ها را از دیتابیس واکشی و Cache کند")
    void shouldFetchCardsFromDatabaseAndCache() throws BadRequestException {

        // Given -
        CardEntity card = CardEntity.builder()
                .cardNumber("6273535555555555")
                .cardType(CardType.DEBIT)
                .active(true)
                .expirationMonth("03")
                .expirationYear("1408")
                .account(testAccount)
                .issuer(testIssuer)
                .build();

        cardRepository.saveAndFlush(card);
        entityManager.clear();

        // When
        Set<CardEntity> cards = inMemoryRepository.getCardsByNationalCode("1234567890");

        // Then
        assertThat(cards).hasSize(1);
        assertThat(cards.iterator().next().getCardNumber()).isEqualTo("6273535555555555");

        // Verify
        Set<CardEntity> cachedCards = inMemoryRepository.getCardsByNationalCode("1234567890");
        assertThat(cachedCards).hasSize(1);
    }

    @Test
    @Order(8)
    @DisplayName("باید لیست خالی برگرداند برای شخصی که کارت ندارد")
    void shouldReturnEmptySetForPersonWithoutCards() {
        // Given - شخص جدید بدون کارت
        PersonEntity personWithoutCard = PersonEntity.builder()
                .nationalCode("9876543210")
                .firstName("محمد")
                .lastName("رضایی")
                .phone("09351234567")
                .address("اصفهان")
                .build();
        entityManager.persistAndFlush(personWithoutCard);

        // When
        Set<CardEntity> cards = inMemoryRepository.getCardsByNationalCode("9876543210");

        // Then
        assertThat(cards).isEmpty();
    }

    @Test
    @Order(9)
    @DisplayName("باید چندین کارت مختلف را مدیریت کند")
    void shouldHandleMultipleCards() throws BadRequestException {

        // Given - دو کارت مختلف
        IssuerEntity newIssuer1 = IssuerEntity.builder()
                .issuerCode("603796")
                .name("بانک سامان")
                .build();
        newIssuer1 = entityManager.persistAndFlush(newIssuer1);
        CardEntity debitCard = CardEntity.builder()
                .cardNumber("6273531111111111")
                .cardType(CardType.DEBIT)
                .active(true)
                .expirationMonth("12")
                .expirationYear("1405")
                .account(testAccount)
                .issuer(newIssuer1)
                .build();

        // صادرکننده جدید
        IssuerEntity newIssuer2 = IssuerEntity.builder()
                .issuerCode("603799")
                .name("بانک ملی")
                .build();
        newIssuer2 = entityManager.persistAndFlush(newIssuer2);

        CardEntity creditCard = CardEntity.builder()
                .cardNumber("6037992222222222")
                .cardType(CardType.CREDIT)
                .active(true)
                .expirationMonth("06")
                .expirationYear("1406")
                .account(testAccount)
                .issuer(newIssuer2)
                .build();

        // When
        inMemoryRepository.saveCard(debitCard);
        inMemoryRepository.saveCard(creditCard);

        // Then
        Set<CardEntity> cards = inMemoryRepository.getCardsByNationalCode("1234567890");
        assertThat(cards).hasSize(2);


        assertThat(cardRepository.findByCardNumber("6273531111111111")).isPresent();
        assertThat(cardRepository.findByCardNumber("6037992222222222")).isPresent();
    }

    // ========== Clear Tests ==========

    @Test
    @Order(10)
    @DisplayName("باید فقط درون مخزنی را پاک کند نه دیتابیس")
    void shouldClearOnlyInMemoryNotDatabase() throws BadRequestException {
        // Given - یک کارت ذخیره می‌کنیم
        CardEntity card = CardEntity.builder()
                .cardNumber("6273533333333333")
                .cardType(CardType.DEBIT)
                .active(true)
                .expirationMonth("09")
                .expirationYear("1407")
                .account(testAccount)
                .issuer(testIssuer)
                .build();

        inMemoryRepository.saveCard(card);

        // When
        inMemoryRepository.clearAll();

        // Then
        Optional<CardEntity> dbCard = cardRepository.findByCardNumber("6273533333333333");
        assertThat(dbCard).isPresent();

        Map<String, Integer> stats = inMemoryRepository.getStatistics();
        assertThat(stats.get("cards")).isZero();
    }

    @Test
    @Order(11)
    @DisplayName("باید Cache و دیتابیس را با هم پاک کند")
    void shouldClearBothCacheAndDatabase() throws BadRequestException {
        // Given
        CardEntity card = CardEntity.builder()
                .cardNumber("6273534444444444")
                .cardType(CardType.CREDIT)
                .active(true)
                .expirationMonth("11")
                .expirationYear("1408")
                .account(testAccount)
                .issuer(testIssuer)
                .build();

        inMemoryRepository.saveCard(card);

        // When
        inMemoryRepository.clearAllIncludingDatabase();

        // Then -
        assertThat(cardRepository.findAll()).isEmpty();
        assertThat(accountRepository.findAll()).isEmpty();
        assertThat(personRepository.findAll()).isEmpty();
        assertThat(issuerRepository.findAll()).isEmpty();

        // And Cache هم خالی است
        Map<String, Integer> stats = inMemoryRepository.getStatistics();
        assertThat(stats.get("persons")).isZero();
        assertThat(stats.get("cards")).isZero();
    }

    @Test
    @Order(13)
    @DisplayName("باید خطا بدهد برای کارت با account null")
    void shouldThrowExceptionForCardWithNullAccount() {
        // Given
        CardEntity invalidCard = CardEntity.builder()
                .cardNumber("6273537777777777")
                .cardType(CardType.DEBIT)
                .active(true)
                .expirationMonth("12")
                .expirationYear("1405")
                .account(null)  // null
                .issuer(testIssuer)
                .build();

        // When & Then
        assertThatThrownBy(() -> inMemoryRepository.saveCard(invalidCard))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("حساب کارت نمی تواند خالی باشد");
    }

    @Test
    @Order(14)
    @DisplayName("باید خطا بدهد برای کارت با issuer null")
    void shouldThrowExceptionForCardWithNullIssuer() {
        // Given
        CardEntity invalidCard = CardEntity.builder()
                .cardNumber("6273538888888888")
                .cardType(CardType.CREDIT)
                .active(true)
                .expirationMonth("12")
                .expirationYear("1405")
                .account(testAccount)
                .issuer(null)  // null
                .build();

        // When & Then
        assertThatThrownBy(() -> inMemoryRepository.saveCard(invalidCard))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("صادرکننده نمی تواند خالی باشد");
    }
}
