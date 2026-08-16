package com.isc.cardmanagement.monitoring;

import com.isc.cardmanagement.repository.InMemoryRepository;
import com.isc.cardmanagement.repository.jpa.AccountRepository;
import com.isc.cardmanagement.repository.jpa.CardRepository;
import com.isc.cardmanagement.repository.jpa.IssuerRepository;
import com.isc.cardmanagement.repository.jpa.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class MemoryUsageLogger {

    private final InMemoryRepository inMemoryRepository;

    private final PersonRepository personRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final IssuerRepository issuerRepository;

    @Scheduled(initialDelay = 60_000, fixedDelay = 600_000)
    public void reportMemoryUsage() {
        try {
            printMemoryUsage("Thread Memory Report");
            printEntityStats();
        } catch (Exception e) {
            log.error("خطا در گزارش مصرف حافظه: {}", e.getMessage(), e);
        }
    }

    private void printMemoryUsage(String stage) {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        long max = runtime.maxMemory();

        log.info("====================== {} ======================", stage);
        log.info("Total Memory: {:.2f} MB", total / (1024.0 * 1024));
        log.info("Free Memory: {:.2f} MB", free / (1024.0 * 1024));
        log.info("Used Memory: {:.2f} MB", used / (1024.0 * 1024));
        log.info("Max Memory: {:.2f} MB", max / (1024.0 * 1024));
    }

    private void printEntityStats() {
        log.info("========== Entity Counts (From H2) ==========");
        log.info("Persons: {}", personRepository.count());
        log.info("Accounts: {}", accountRepository.count());
        log.info("Cards: {}", cardRepository.count());
        log.info("Issuers: {}", issuerRepository.count());
        log.info("========== In-Memory Map ==========");
        log.info("NationalCode Map Entries: {}", inMemoryRepository.getAll().size());
    }
}
