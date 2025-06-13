package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NonScannableItemResponse {
    @JsonProperty("document_control_number")
    private String documentControlNumber;

    @JsonProperty("item_type")
    private String itemType;
}
