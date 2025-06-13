package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SearchResult<T> {
    @JsonProperty("count")
    private int count;

    @JsonProperty("data")
    private List<T> data;
}
