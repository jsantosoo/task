package textrepositoryapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistoryControllerTest {

    @Mock
    private HistoryService historyService;

    @InjectMocks
    private HistoryController historyController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getHistory_shouldReturnResults_whenServiceProvidesData() {
        // Arrange
        List<ComputationResult> mockResults = List.of(mock(ComputationResult.class), mock(ComputationResult.class));
        when(historyService.getLast10Results()).thenReturn(mockResults);

        // Act
        List<ComputationResult> results = historyController.getHistory();

        // Assert
        assertEquals(mockResults.size(), results.size(), "Expected size of results to match mock results");
        verify(historyService, times(1)).getLast10Results();
    }

    @Test
    void getHistory_shouldHandleException_whenServiceThrowsError() {
        // Arrange
        when(historyService.getLast10Results()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> historyController.getHistory());
        assertTrue(exception.getMessage().contains("Failed to fetch last 10 computation results"),
            "Expected exception message to indicate failure in fetching results");
    }
}