package textprocessingapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        kafkaProducerService = new KafkaProducerService(kafkaTemplate);
    }

    @Test
    void sendResult_sendsSerializedJson() throws Exception {
        // Arrange
        Map<String, Object> result = new HashMap<>();
        result.put("foo", "bar");
        ArgumentCaptor<Message<String>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(messageCaptor.capture())).thenReturn(future);

        setTopicField("text-processing-results");

        // Act
        kafkaProducerService.sendResult(result, "key1");

        // Assert
        Message<String> sentMessage = messageCaptor.getValue();
        assertMessageHeaders(sentMessage, "text-processing-results", "key1");
        assertPayloadContains(sentMessage, "foo");
    }

    @Test
    void sendResult_throwsOnSerializationError() {
        // Arrange
        Map<String, Object> result = new HashMap<>() {
            @Override
            public String toString() {
                throw new RuntimeException("fail");
            }
        };

        // Act & Assert
        assertThrows(RuntimeException.class, () -> kafkaProducerService.sendResult(result, "key2"));
    }

    private void setTopicField(String topic) throws Exception {
        java.lang.reflect.Field topicField = KafkaProducerService.class.getDeclaredField("topic");
        topicField.setAccessible(true);
        topicField.set(kafkaProducerService, topic);
    }

    private void assertMessageHeaders(Message<String> message, String expectedTopic, String expectedKey) {
        assertEquals(expectedTopic, message.getHeaders().get(KafkaHeaders.TOPIC));
        assertEquals(expectedKey, message.getHeaders().get(KafkaHeaders.KEY));
    }

    private void assertPayloadContains(Message<String> message, String expectedContent) {
        assertInstanceOf(String.class, message.getPayload());
        assertTrue(message.getPayload().contains(expectedContent));
    }
}