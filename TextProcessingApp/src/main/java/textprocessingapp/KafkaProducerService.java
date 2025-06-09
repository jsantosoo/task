package textprocessingapp;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.kafka.topic:words.processed}")
    private String topic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendResult(Map<String, Object> result, String key) {
        String json = serializeResult(result, key);
        log.info("Sending result to Kafka topic '{}', key='{}': {}", topic, key, json);
        kafkaTemplate.send(
            MessageBuilder.withPayload(json)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key)
                .build()
        ).whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                log.error("Failed to send message to topic '{}', key='{}': {}", topic, key, throwable.getMessage(), throwable);
            } else {
                log.info("Message sent successfully to topic '{}', key='{}'", topic, key);
            }
        });
    }

    private String serializeResult(Map<String, Object> result, String key) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize result for key '{}': {}", key, e.getMessage(), e);
            throw new RuntimeException("Failed to serialize result for Kafka", e);
        }
    }
}
