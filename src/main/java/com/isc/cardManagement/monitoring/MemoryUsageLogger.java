package com.isc.cardManagement.monitoring;

import com.isc.cardManagement.repository.InMemoryRepository;
import com.isc.cardManagement.repository.jpa.AccountRepository;
import com.isc.cardManagement.repository.jpa.CardRepository;
import com.isc.cardManagement.repository.jpa.IssuerRepository;
import com.isc.cardManagement.repository.jpa.PersonRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemoryUsageLogger {

    private final InMemoryRepository inMemoryRepository;

    private final PersonRepository personRepository;
    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final IssuerRepository issuerRepository;

    private Thread reporterThread;

    @PostConstruct
    public void startReportingThread() {
        reporterThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    printMemoryUsage("🧵 Thread Memory Report");
                    printEntityStats();

                    Thread.sleep(600_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // نخ رو مجددا قطع کن
                } catch (Exception e) {
                    System.err.println("خطا در گزارش مصرف حافظه: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        reporterThread.setDaemon(true);
        reporterThread.start();
    }

    @PreDestroy
    public void stopReportingThread() {
        if (reporterThread != null && reporterThread.isAlive()) {
            reporterThread.interrupt();
            try {
                reporterThread.join(5000); // حداکثر 5 ثانیه منتظر می‌ماند برای پایان نخ
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("قطع نخ گزارش‌گیری به درستی انجام نشد.");
            }
        }
    }

    private void printMemoryUsage(String stage) {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        long max = runtime.maxMemory();

        System.out.printf("%n====================== %s ======================%n", stage);
        System.out.printf("🧠 Total Memory: %.2f MB%n", total / (1024.0 * 1024));
        System.out.printf("🟢 Free Memory: %.2f MB%n", free / (1024.0 * 1024));
        System.out.printf("🔴 Used Memory: %.2f MB%n", used / (1024.0 * 1024));
        System.out.printf("📈 Max Memory: %.2f MB%n", max / (1024.0 * 1024));
    }

    private void printEntityStats() {
        System.out.println("========== 📊 Entity Counts (From H2) ==========");
        System.out.println(" Persons: " + personRepository.count());
        System.out.println(" Accounts: " + accountRepository.count());
        System.out.println(" Cards: " + cardRepository.count());
        System.out.println(" Issuers: " + issuerRepository.count());
        System.out.println("========== 📦 In-Memory Map ==========");
        System.out.println(" NationalCode Map Entries: " + inMemoryRepository.getAll().size());
        System.out.println("=============================================\n");
    }
}

