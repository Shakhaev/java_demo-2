package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.dto.TransactionAcceptEvent;
import ru.t1.java.demo.model.entity.TransactionAccept;
import ru.t1.java.demo.processor.TransactionsExceedLimitProcessor;
import ru.t1.java.demo.repository.TransactionAcceptRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionCountService {

    @Value("${scheduler.transactions-time-limit}")
    private Integer transactionsTimeLimit;
    @Value("${scheduler.transactions-count-limit}")
    private Integer transactionsCountLimit;

    private final TransactionsExceedLimitProcessor transactionsExceedLimitProcessor;
    private final TransactionAcceptRepository transactionAcceptRepository;

    @Transactional
    public void updateTransactionCount(TransactionAcceptEvent transactionAcceptEvent) {
        TransactionAccept transactionAccept = prepareTransactionAccept(transactionAcceptEvent);
        transactionAcceptRepository.save(transactionAccept);
        log.info("Saved transaction: {}", transactionAccept.getTransactionId());
    }

    @Transactional(readOnly = true)
    public void checkQuantityExceedsLimit() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusMinutes(transactionsTimeLimit);

        List<TransactionAccept> transactions = transactionAcceptRepository.findAllByCreatedAtBetweenAndIsBlockedFalse(oneHourAgo, now);

        Map<UUID, Map<UUID, Long>> transactionsCountByClientAndAccount = transactions.stream()
                .collect(Collectors.groupingBy(TransactionAccept::getClientId,
                        Collectors.groupingBy(TransactionAccept::getToAccountId, Collectors.counting())));

        transactionsCountByClientAndAccount.forEach((clientId, accountCounts) -> {
            accountCounts.forEach((toAccountId, count) -> {
                if (count > transactionsCountLimit) {
                    transactionsExceedLimitProcessor.processOverLimitTransactions(transactions, clientId, toAccountId);
                    log.info("Transactions exceeded limit for client {} and account {}", clientId, toAccountId);
                }
            });
        });
    }

    private TransactionAccept prepareTransactionAccept(TransactionAcceptEvent transactionAcceptEvent) {
        return TransactionAccept.builder()
                .clientId(transactionAcceptEvent.getClientId())
                .fromAccountId(transactionAcceptEvent.getFromAccountId())
                .toAccountId(transactionAcceptEvent.getToAccountId())
                .transactionId(transactionAcceptEvent.getTransactionId())
                .createdAt(transactionAcceptEvent.getCreatedAt())
                .transactionAmount(transactionAcceptEvent.getTransactionAmount())
                .fromAccountBalance(transactionAcceptEvent.getFromAccountBalance())
                .isBlocked(false)
                .build();
    }
}
