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
public class ComputationResultResponse {
    @JsonProperty("freq_word")
    private String freq_word;
    @JsonProperty("avg_paragraph_size")
    private double avg_paragraph_size;
    @JsonProperty("avg_paragraph_processing_time")
    private double avg_paragraph_processing_time;
    @JsonProperty("total_processing_time")
    private double total_processing_time;
}
