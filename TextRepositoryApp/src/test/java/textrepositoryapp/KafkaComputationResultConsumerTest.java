package textrepositoryapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaComputationResultConsumerTest {

    @Mock
    private ComputationResultRepository repository;

    @InjectMocks
    private KafkaComputationResultConsumer consumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listen_shouldSaveResult_whenRecordIsValid() throws Exception {
        // Arrange
        ComputationResultDTO dto = ComputationResultDTO.builder()
            .freqWord("test")
            .avgParagraphSize(5.0)
            .avgParagraphProcessingTime(10.0)
            .totalProcessingTime(100.0)
            .build();
        String value = objectMapper.writeValueAsString(dto);
        ConsumerRecord<String, String> record = new ConsumerRecord<>("words.processed", 0, 0L, "key", value);

        // Act
        consumer.listen(record);

        // Assert
        verify(repository, times(1)).save(any(ComputationResult.class));
    }

    @Test
    void listen_shouldDoNothing_whenRecordIsEmpty() {
        // Arrange
        ConsumerRecord<String, String> record = new ConsumerRecord<>("words.processed", 0, 0L, "key", "");

        // Act
        consumer.listen(record);

        // Assert
        verify(repository, never()).save(any());
    }

    @Test
    void listen_shouldThrowException_whenJsonIsInvalid() {
        // Arrange
        ConsumerRecord<String, String> record = new ConsumerRecord<>("words.processed", 0, 0L, "key", "not-json");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> consumer.listen(record));
        assertTrue(exception.getMessage().contains("Failed to process Kafka record"),
            "Expected exception message to indicate failure in processing record");
    }
}