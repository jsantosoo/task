package textprocessingapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class TextProcessingController {

    @Autowired
    private TextProcessingService textProcessingService;

    @GetMapping("/betvictor/text")
    public ResponseEntity<Map<String, Object>> processText(@RequestParam(name = "p") int paragraphs) {
        log.info("Received request to process {} paragraphs", paragraphs);
        if (paragraphs <= 0) {
            log.warn("Invalid parameter 'p': {}. Must be greater than 0.", paragraphs);
            return ResponseEntity.badRequest().body(Map.of("error", "Parameter 'p' must be greater than 0"));
        }
        try {
            Map<String, Object> result = textProcessingService.processParagraphs(paragraphs);
            log.info("Successfully processed {} paragraphs", paragraphs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Exception while processing paragraphs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }
}

