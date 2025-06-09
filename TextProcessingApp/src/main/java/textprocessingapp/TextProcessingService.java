package textprocessingapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class TextProcessingService {
    private final WebClient webClient;

    @Autowired
    public TextProcessingService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Autowired public KafkaProducerService kafkaProducerService;

    @Value("${app.hipsum.api.url}")
    private String hipsumApiUrl;

    public Map<String, Object> processParagraphs(int paragraphs) {
        log.info("Starting to process {} paragraphs", paragraphs);
        List<String> paragraphList = new ArrayList<>();
        List<Long> processingTimes = new ArrayList<>();
        Instant totalStart = Instant.now();

        for (int i = 0; i < paragraphs; i++) {
            ParagraphResult result = fetchParagraphWithTiming(i + 1);
            paragraphList.add(result.paragraph);
            processingTimes.add(result.processingTimeMs);
        }

        String mostFrequentWord = findMostFrequentWord(paragraphList);
        double avgParagraphSize = average(paragraphList.stream().mapToInt(String::length).toArray());
        double avgProcessingTime = average(processingTimes.stream().mapToLong(Long::longValue).toArray());
        long totalProcessingTime = Duration.between(totalStart, Instant.now()).toMillis();

        log.info("Most frequent word: {}", mostFrequentWord);
        log.info("Average paragraph size: {}", avgParagraphSize);
        log.info("Average paragraph processing time: {} ms", avgProcessingTime);
        log.info("Total processing time: {} ms", totalProcessingTime);

        Map<String, Object> result = Map.of(
                "freq_word", mostFrequentWord,
                "avg_paragraph_size", avgParagraphSize,
                "avg_paragraph_processing_time", avgProcessingTime,
                "total_processing_time", totalProcessingTime
        );

        kafkaProducerService.sendResult(result, mostFrequentWord);
        log.info("Result sent to Kafka with key '{}'.", mostFrequentWord);
        return result;
    }

    private static double average(int[] values) {
        return values.length == 0 ? 0 : Arrays.stream(values).average().orElse(0);
    }

    private static double average(long[] values) {
        return values.length == 0 ? 0 : Arrays.stream(values).average().orElse(0);
    }

    private ParagraphResult fetchParagraphWithTiming(int index) {
        Instant start = Instant.now();
        String paragraph = null;
        try {
            paragraph = fetchParagraph();
        } catch (Exception e) {
            log.error("Exception while fetching paragraph {}: {}", index, e.getMessage(), e);
            paragraph = "";
        }
        Instant end = Instant.now();
        long duration = Duration.between(start, end).toMillis();
        log.debug("Fetched paragraph {}: {}", index, paragraph);
        log.debug("Processing time for paragraph {}: {} ms", index, duration);
        return new ParagraphResult(paragraph, duration);
    }

    private static class ParagraphResult {
        final String paragraph;
        final long processingTimeMs;
        ParagraphResult(String paragraph, long processingTimeMs) {
            this.paragraph = paragraph;
            this.processingTimeMs = processingTimeMs;
        }
    }

    private String fetchParagraph() {
        try {
            @SuppressWarnings("unchecked")
            List<String> response = (List<String>) webClient.get()
                    .uri(hipsumApiUrl)
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();
            log.debug("Fetched response from API: {}", response);
            return response != null && !response.isEmpty() ? response.get(0) : "";
        } catch (Exception e) {
            log.error("Error fetching paragraph from API: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String findMostFrequentWord(List<String> paragraphs) {
        Map<String, Integer> freq = new HashMap<>();
        Pattern wordPattern = Pattern.compile("\\b\\w+\\b");
        for (String paragraph : paragraphs) {
            Matcher matcher = wordPattern.matcher(paragraph.toLowerCase());
            while (matcher.find()) {
                String word = matcher.group();
                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }
        String mostFrequent = freq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
        log.debug("Most frequent word calculated: {}", mostFrequent);
        return mostFrequent;
    }
}
