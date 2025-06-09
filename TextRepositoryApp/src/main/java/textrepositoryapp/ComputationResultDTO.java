package textrepositoryapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComputationResultDTO {
    @JsonProperty("freq_word")
    private String freqWord;
    @JsonProperty("avg_paragraph_size")
    private double avgParagraphSize;
    @JsonProperty("avg_paragraph_processing_time")
    private double avgParagraphProcessingTime;
    @JsonProperty("total_processing_time")
    private double totalProcessingTime;
}
