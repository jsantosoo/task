package textrepositoryapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/betvictor")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/history")
    public List<ComputationResult> getHistory() {
        log.info("Fetching last 10 computation results from service.");
        try {
            List<ComputationResult> results = historyService.getLast10Results();
            log.info("Successfully fetched {} results.", results.size());
            return results;
        } catch (Exception e) {
            log.error("Error fetching last 10 computation results", e);
            throw new RuntimeException("Failed to fetch last 10 computation results", e);
        }
    }
}
