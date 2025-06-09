package textprocessingapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TextProcessingControllerTest {

    @Mock
    private TextProcessingService textProcessingService;

    @InjectMocks
    private TextProcessingController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processText_returnsBadRequestForInvalidParam() {
        // Act
        ResponseEntity<Map<String, Object>> response = controller.processText(0);

        // Assert
        assertResponse(response, 400, "error");
    }

    @Test
    void processText_returnsOkForValidParam() {
        // Arrange
        when(textProcessingService.processParagraphs(1)).thenReturn(Map.of("result", "ok"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.processText(1);

        // Assert
        assertResponse(response, 200, "result");
    }

    @Test
    void processText_returnsServerErrorOnException() {
        // Arrange
        when(textProcessingService.processParagraphs(2)).thenThrow(new RuntimeException("fail"));

        // Act
        ResponseEntity<Map<String, Object>> response = controller.processText(2);

        // Assert
        assertResponse(response, 500, "error");
    }

    private void assertResponse(ResponseEntity<Map<String, Object>> response, int expectedStatus, String expectedKey) {
        assertEquals(expectedStatus, response.getStatusCodeValue());
        assertTrue(response.getBody().containsKey(expectedKey));
    }
}