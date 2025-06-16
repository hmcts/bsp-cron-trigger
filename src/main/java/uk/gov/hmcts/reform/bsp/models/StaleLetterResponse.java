package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class StaleLetterResponse {

    public final int count;

    @JsonProperty("stale_letters")
    public final List<StaleLetter> staleLetters;

    /**
     * Constructor here is needed, because the two variables above are final.
     */
    @JsonCreator
    public StaleLetterResponse(
        @JsonProperty("count") int count,
        @JsonProperty("stale_letters") List<StaleLetter> staleLetters
    ) {
        this.count = count;
        this.staleLetters = staleLetters;
    }
}
