package ru.t1.java.demo.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.dto.TransactionResultEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTransactionResultProducer<T extends TransactionResultEvent> {

    private final KafkaTemplate<String, TransactionResultEvent> kafkaTransactionResultTemplate;

    public void send(TransactionResultEvent event) {
        try {
            kafkaTransactionResultTemplate.sendDefault(UUID.randomUUID().toString(), event).get();

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            kafkaTransactionResultTemplate.flush();
        }
    }

    public void sendTo(String topic, TransactionResultEvent event) {
        try {
            kafkaTransactionResultTemplate.send(topic, event).get();
            kafkaTransactionResultTemplate.send(topic,
                            1,
                            LocalDateTime.now().toEpochSecond(ZoneOffset.of("+03:00")),
                            UUID.randomUUID().toString(),
                            event)
                    .get();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            kafkaTransactionResultTemplate.flush();
        }
    }
}
