package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class EnvelopeResponse {

    private UUID id;

    @JsonProperty("case_number")
    private String caseNumber;

    private String container;

    @JsonProperty("po_box")
    private String poBox;

    private String jurisdiction;

    @JsonProperty("delivery_date")
    private Instant deliveryDate;

    @JsonProperty("opening_date")
    private Instant openingDate;

    @JsonProperty("zip_file_createddate")
    private Instant zipFileCreatedDate;

    @JsonProperty("zip_file_name")
    private String zipFileName;

    @JsonProperty("rescan_for")
    private String rescanFor;

    private Status status;

    private String classification;

    @JsonProperty("scannable_items")
    private List<ScannableItemResponse> scannableItems;

    private List<PaymentResponse> payments;

    @JsonProperty("non_scannable_items")
    private List<NonScannableItemResponse> nonScannableItems;

    @JsonProperty("ccd_id")
    private String ccdId;

    @JsonProperty("ccd_action")
    private String envelopeCcdAction;
}
