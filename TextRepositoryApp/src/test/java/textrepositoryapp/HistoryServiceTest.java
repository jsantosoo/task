package textrepositoryapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistoryServiceTest {

    @Mock
    private ComputationResultRepository repository;

    @InjectMocks
    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLast10Results_shouldReturnResults_whenRepositoryProvidesData() {
        // Arrange
        List<ComputationResult> mockResults = List.of(mock(ComputationResult.class), mock(ComputationResult.class));
        when(repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10))).thenReturn(mockResults);

        // Act
        List<ComputationResult> results = historyService.getLast10Results();

        // Assert
        assertEquals(mockResults.size(), results.size(), "Expected size of results to match mock results");
        verify(repository, times(1)).findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));
    }

    @Test
    void getLast10Results_shouldHandleException_whenRepositoryThrowsError() {
        // Arrange
        when(repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10))).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> historyService.getLast10Results());
        assertTrue(exception.getMessage().contains("Could not fetch last 10 computation results"),
            "Expected exception message to indicate failure in fetching results");
    }
}