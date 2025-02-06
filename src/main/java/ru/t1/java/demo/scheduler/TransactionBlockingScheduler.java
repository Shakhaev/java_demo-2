package ru.t1.java.demo.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.service.TransactionCountService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionBlockingScheduler {

    private final TransactionCountService transactionCountService;

    @Scheduled(cron = "${scheduler.cron}", zone = "${scheduler.zone}")
    public void checkTransactions() {
        transactionCountService.checkQuantityExceedsLimit();
        log.debug("Check for exceeded transaction limit started.");
    }
}
