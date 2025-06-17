package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class ScannableItemResponse {

    @JsonProperty("document_control_number")
    private String documentControlNumber;

    @JsonProperty("scanning_date")
    private Instant scanningDate;

    @JsonProperty("ocr_accuracy")
    private String ocrAccuracy;

    @JsonProperty("manual_intervention")
    private String manualIntervention;

    @JsonProperty("next_action")
    private String nextAction;

    @JsonProperty("next_action_date")
    private Instant nextActionDate;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("document_uuid")
    private String documentUuid;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("document_subtype")
    private String documentSubtype;

    @JsonProperty("has_ocr_data")
    private boolean hasOcrData;
}
