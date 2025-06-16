package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ScannableItemResponseTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Should deserialize all fields from JSON")
    void jsonDeserialization_shouldPopulateAllFields() throws Exception {
        Instant scanDate = Instant.parse("2025-06-15T10:15:30Z");
        Instant nextActionDate = Instant.parse("2025-06-16T11:20:45Z");

        String json = """
            {
              "document_control_number":"DCN123",
              "scanning_date":"%s",
              "ocr_accuracy":"98.7",
              "manual_intervention":"NO",
              "next_action":"RETRY",
              "next_action_date":"%s",
              "file_name":"scan1.tiff",
              "document_uuid":"uuid-1234",
              "document_type":"FORM",
              "document_subtype":"A",
              "has_ocr_data":true
            }
            """.formatted(scanDate, nextActionDate);

        ScannableItemResponse resp = mapper.readValue(json, ScannableItemResponse.class);

        assertThat(resp.getDocumentControlNumber()).isEqualTo("DCN123");
        assertThat(resp.getScanningDate()).isEqualTo(scanDate);
        assertThat(resp.getOcrAccuracy()).isEqualTo("98.7");
        assertThat(resp.getManualIntervention()).isEqualTo("NO");
        assertThat(resp.getNextAction()).isEqualTo("RETRY");
        assertThat(resp.getNextActionDate()).isEqualTo(nextActionDate);
        assertThat(resp.getFileName()).isEqualTo("scan1.tiff");
        assertThat(resp.getDocumentUuid()).isEqualTo("uuid-1234");
        assertThat(resp.getDocumentType()).isEqualTo("FORM");
        assertThat(resp.getDocumentSubtype()).isEqualTo("A");
        assertThat(resp.isHasOcrData()).isTrue();
    }

    @Test
    @DisplayName("Should serialize back to JSON with the same field values")
    void jsonSerialization_roundTripsCorrectly() throws Exception {
        Instant scanDate = Instant.parse("2025-06-15T10:15:30Z");
        Instant nextActionDate = Instant.parse("2025-06-16T11:20:45Z");

        ScannableItemResponse resp = getScannableItemResponse(scanDate, nextActionDate);

        String serialized = mapper.writeValueAsString(resp);
        var node = mapper.readTree(serialized);

        assertThat(node.get("document_control_number").asText()).isEqualTo("DCN123");
        assertThat(Instant.parse(node.get("scanning_date").asText())).isEqualTo(scanDate);
        assertThat(node.get("ocr_accuracy").asText()).isEqualTo("98.7");
        assertThat(node.get("manual_intervention").asText()).isEqualTo("NO");
        assertThat(node.get("next_action").asText()).isEqualTo("RETRY");
        assertThat(Instant.parse(node.get("next_action_date").asText())).isEqualTo(nextActionDate);
        assertThat(node.get("file_name").asText()).isEqualTo("scan1.tiff");
        assertThat(node.get("document_uuid").asText()).isEqualTo("uuid-1234");
        assertThat(node.get("document_type").asText()).isEqualTo("FORM");
        assertThat(node.get("document_subtype").asText()).isEqualTo("A");
        assertThat(node.get("has_ocr_data").asBoolean()).isTrue();
    }

    @NotNull
    private static ScannableItemResponse getScannableItemResponse(Instant scanDate, Instant nextActionDate) {
        ScannableItemResponse resp = new ScannableItemResponse();
        resp.setDocumentControlNumber("DCN123");
        resp.setScanningDate(scanDate);
        resp.setOcrAccuracy("98.7");
        resp.setManualIntervention("NO");
        resp.setNextAction("RETRY");
        resp.setNextActionDate(nextActionDate);
        resp.setFileName("scan1.tiff");
        resp.setDocumentUuid("uuid-1234");
        resp.setDocumentType("FORM");
        resp.setDocumentSubtype("A");
        resp.setHasOcrData(true);
        return resp;
    }
}
