package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.kafka.producer.KafkaTransactionResultProducer;
import ru.t1.java.demo.model.dto.TransactionAcceptEvent;
import ru.t1.java.demo.model.dto.TransactionResultEvent;
import ru.t1.java.demo.model.enums.TransactionStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessingService {

    private final KafkaTransactionResultProducer<TransactionResultEvent> kafkaTransactionResultProducer;
    private final TransactionCountService transactionCountService;

    public void processTransaction(TransactionAcceptEvent transactionAcceptEvent) {
        if (transactionAcceptEvent.getTransactionAmount().compareTo(transactionAcceptEvent.getFromAccountBalance()) > 0) {
            sendTransactionResultToKafka(transactionAcceptEvent, TransactionStatus.REJECTED);
        } else {
            transactionCountService.updateTransactionCount(transactionAcceptEvent);
            sendTransactionResultToKafka(transactionAcceptEvent, TransactionStatus.ACCEPTED);
        }
    }

    private void sendTransactionResultToKafka(TransactionAcceptEvent transactionAcceptEvent, TransactionStatus status) {
        TransactionResultEvent event = TransactionResultEvent.builder()
                .toAccountId(transactionAcceptEvent.getToAccountId())
                .fromAccountId(transactionAcceptEvent.getFromAccountId())
                .transactionId(transactionAcceptEvent.getTransactionId())
                .status(status)
                .build();
        kafkaTransactionResultProducer.send(event);
    }
}
