package ru.t1.java.demo.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.kafka.producer.KafkaTransactionResultProducer;
import ru.t1.java.demo.model.dto.TransactionResultEvent;
import ru.t1.java.demo.model.entity.TransactionAccept;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.repository.TransactionAcceptRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionsExceedLimitProcessor {

    private final KafkaTransactionResultProducer<TransactionResultEvent> kafkaTransactionResultProducer;
    private final TransactionAcceptRepository transactionAcceptRepository;

    @Transactional
    public void processOverLimitTransactions(List<TransactionAccept> transactions, UUID clientId, UUID toAccountId) {
        List<TransactionAccept> exceededTransactions = transactions.stream()
                .filter(t -> t.getClientId().equals(clientId) && t.getToAccountId().equals(toAccountId))
                .toList();

        exceededTransactions.forEach(this::processTransaction);
    }

    @Async("taskExecutor")
    public void processTransaction(TransactionAccept transaction) {
        sendTransactionResultToKafka(transaction);
        blockTransaction(transaction);
    }

    private void sendTransactionResultToKafka(TransactionAccept transaction) {
        TransactionResultEvent event = TransactionResultEvent.builder()
                .toAccountId(transaction.getToAccountId())
                .fromAccountId(transaction.getFromAccountId())
                .transactionId(transaction.getTransactionId())
                .status(TransactionStatus.BLOCKED)
                .build();
        kafkaTransactionResultProducer.send(event);
        log.info("Sent transaction result to Kafka: {}", event);
    }

    private void blockTransaction(TransactionAccept transaction) {
        transaction.setIsBlocked(true);
        transactionAcceptRepository.save(transaction);
        log.info("Blocked transaction: {}", transaction.getTransactionId());
    }
}
