package textrepositoryapp;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class HistoryService {
    private final ComputationResultRepository repository;

    public HistoryService(ComputationResultRepository repository) {
        this.repository = repository;
    }

    public List<ComputationResult> getLast10Results() {
        try {
            log.info("Fetching last 10 computation results from repository.");
            List<ComputationResult> results = repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));
            log.info("Fetched {} results.", results.size());
            return results;
        } catch (Exception e) {
            log.error("Failed to fetch last 10 computation results", e);
            throw new RuntimeException("Could not fetch last 10 computation results", e);
        }
    }
}
