package textprocessingapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TextProcessingServiceTest {

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private TextProcessingService textProcessingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        textProcessingService = new TextProcessingService(webClient);
        textProcessingService.kafkaProducerService = kafkaProducerService;
        setField("hipsumApiUrl", "http://mocked-url");
    }

    @Test
    void processParagraphs_returnsResult() {
        mockWebClientResponse(List.of("test test paragraph"));
        Map<String, Object> result = textProcessingService.processParagraphs(1);
        assertResult(result, "test", "test test paragraph".length());
        verifyKafkaSend("test");
    }

    @Test
    void processParagraphs_handlesApiErrorGracefully() {
        mockWebClientError();
        Map<String, Object> result = textProcessingService.processParagraphs(1);
        assertResult(result, "", 0);
        verifyKafkaSend("");
    }

    private void setField(String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = TextProcessingService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(textProcessingService, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void mockWebClientResponse(List<String> mockResponse) {
        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(String.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(Mono.just(mockResponse)).when(responseSpec).bodyToMono(List.class);
    }

    private void mockWebClientError() {
        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(String.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(Mono.error(new WebClientRequestException(new RuntimeException("API down"), HttpMethod.GET, URI.create("http://localhost"), new org.springframework.http.HttpHeaders()))).when(responseSpec).bodyToMono(List.class);
    }

    private void assertResult(Map<String, Object> result, String expectedFreqWord, double expectedAvgSize) {
        assertNotNull(result);
        assertEquals(expectedFreqWord, result.get("freq_word"));
        assertEquals(expectedAvgSize, result.get("avg_paragraph_size"));
        assertTrue((double) result.get("avg_paragraph_processing_time") >= 0);
        assertTrue((long) result.get("total_processing_time") >= 0);
    }

    private void verifyKafkaSend(String expectedKey) {
        verify(kafkaProducerService, times(1)).sendResult(any(), eq(expectedKey));
    }

}