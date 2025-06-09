package textrepositoryapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Instant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaComputationResultConsumer {

    private final ComputationResultRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaComputationResultConsumer(ComputationResultRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(
        topics = "words.processed",
        groupId = "repository-app-group",
        concurrency = "${kafka.consumer.concurrency:1}"
    )
    public void listen(ConsumerRecord<String, String> record) {
        log.info("Received record: key={}, value={}", record.key(), record.value());

        if (record.value() == null || record.value().isEmpty()) {
            log.info("Received empty or null record, skipping.");
            return;
        }

        try {
            ComputationResultDTO dto = objectMapper.readValue(record.value(), ComputationResultDTO.class);
            ComputationResult result = ComputationResult.builder()
                .freqWord(dto.getFreqWord())
                .avgParagraphSize(dto.getAvgParagraphSize())
                .avgParagraphProcessingTime(dto.getAvgParagraphProcessingTime())
                .totalProcessingTime(dto.getTotalProcessingTime())
                .createdAt(Instant.now())
                .build();
            repository.save(result);
            log.info("Saved ComputationResult: {}", result);
        } catch (IOException e) {
            log.error("Failed to deserialize or save record: {}", record.value(), e);
            throw new RuntimeException("Failed to process Kafka record: " + record.value(), e);
        } catch (Exception e) {
            log.error("Unexpected error while processing record: {}", record.value(), e);
            throw new RuntimeException("Unexpected error while processing Kafka record: " + record.value(), e);
        }
    }
}
