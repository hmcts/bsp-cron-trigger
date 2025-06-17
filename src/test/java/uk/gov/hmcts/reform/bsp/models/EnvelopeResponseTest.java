package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeResponseTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper()
            .findAndRegisterModules()                 // support Instant
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Lombok getters/setters work")
    void lombokAccessors() {
        EnvelopeResponse resp = new EnvelopeResponse();

        UUID id = UUID.randomUUID();
        Instant d1 = Instant.parse("2025-01-02T00:00:00Z");
        Instant d2 = Instant.parse("2025-01-03T00:00:00Z");
        Instant d3 = Instant.parse("2025-01-04T00:00:00Z");

        resp.setId(id);
        resp.setCaseNumber("case123");
        resp.setContainer("cont");
        resp.setPoBox("PO123");
        resp.setJurisdiction("J1");
        resp.setDeliveryDate(d1);
        resp.setOpeningDate(d2);
        resp.setZipFileCreatedDate(d3);
        resp.setZipFileName("zipname");
        resp.setRescanFor("scan");
        resp.setStatus(EnvelopeStatus.COMPLETED);
        resp.setClassification("A");
        resp.setScannableItems(List.of());
        resp.setPayments(List.of());
        resp.setNonScannableItems(List.of());
        resp.setCcdId("ccd111");
        resp.setEnvelopeCcdAction("actionX");

        assertThat(resp.getId()).isEqualTo(id);
        assertThat(resp.getCaseNumber()).isEqualTo("case123");
        assertThat(resp.getContainer()).isEqualTo("cont");
        assertThat(resp.getPoBox()).isEqualTo("PO123");
        assertThat(resp.getJurisdiction()).isEqualTo("J1");
        assertThat(resp.getDeliveryDate()).isEqualTo(d1);
        assertThat(resp.getOpeningDate()).isEqualTo(d2);
        assertThat(resp.getZipFileCreatedDate()).isEqualTo(d3);
        assertThat(resp.getZipFileName()).isEqualTo("zipname");
        assertThat(resp.getRescanFor()).isEqualTo("scan");
        assertThat(resp.getStatus()).isEqualTo(EnvelopeStatus.COMPLETED);
        assertThat(resp.getClassification()).isEqualTo("A");
        assertThat(resp.getScannableItems()).isEmpty();
        assertThat(resp.getPayments()).isEmpty();
        assertThat(resp.getNonScannableItems()).isEmpty();
        assertThat(resp.getCcdId()).isEqualTo("ccd111");
        assertThat(resp.getEnvelopeCcdAction()).isEqualTo("actionX");
    }

    @Test
    @DisplayName("Serializes to JSON with correct field names")
    void shouldSerializeToExpectedJson() throws Exception {
        EnvelopeResponse resp = new EnvelopeResponse();
        resp.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        resp.setCaseNumber("case123");
        resp.setContainer("cont");
        resp.setPoBox("PO123");
        resp.setJurisdiction("J1");
        resp.setDeliveryDate(Instant.parse("2025-01-02T00:00:00Z"));
        resp.setOpeningDate(Instant.parse("2025-01-03T00:00:00Z"));
        resp.setZipFileCreatedDate(Instant.parse("2025-01-04T00:00:00Z"));
        resp.setZipFileName("zipname");
        resp.setRescanFor("scan");
        resp.setStatus(EnvelopeStatus.COMPLETED);
        resp.setClassification("A");
        resp.setScannableItems(List.of());
        resp.setPayments(List.of());
        resp.setNonScannableItems(List.of());
        resp.setCcdId("ccd111");
        resp.setEnvelopeCcdAction("actionX");

        String json = mapper.writeValueAsString(resp);

        assertThat(json)
            .contains("\"id\":\"123e4567-e89b-12d3-a456-426614174000\"")
            .contains("\"case_number\":\"case123\"")
            .contains("\"container\":\"cont\"")
            .contains("\"po_box\":\"PO123\"")
            .contains("\"jurisdiction\":\"J1\"")
            .contains("\"delivery_date\":\"2025-01-02T00:00:00Z\"")
            .contains("\"opening_date\":\"2025-01-03T00:00:00Z\"")
            .contains("\"zip_file_createddate\":\"2025-01-04T00:00:00Z\"")
            .contains("\"zip_file_name\":\"zipname\"")
            .contains("\"rescan_for\":\"scan\"")
            .contains("\"status\":\"COMPLETED\"")
            .contains("\"classification\":\"A\"")
            .contains("\"scannable_items\":[]")
            .contains("\"payments\":[]")
            .contains("\"non_scannable_items\":[]")
            .contains("\"ccd_id\":\"ccd111\"")
            .contains("\"ccd_action\":\"actionX\"");
    }

    @Test
    @DisplayName("Deserializes from JSON into all fields")
    void shouldDeserializeFromJson() throws Exception {
        String json = """
            {
              "id":"abcdefab-1234-5678-90ab-cdefabcdefab",
              "case_number":"case123",
              "container":"cont",
              "po_box":"PO123",
              "jurisdiction":"J1",
              "delivery_date":"2025-01-02T00:00:00Z",
              "opening_date":"2025-01-03T00:00:00Z",
              "zip_file_createddate":"2025-01-04T00:00:00Z",
              "zip_file_name":"zipname",
              "rescan_for":"scan",
              "status":"COMPLETED",
              "classification":"A",
              "scannable_items":[],
              "payments":[],
              "non_scannable_items":[],
              "ccd_id":"ccd111",
              "ccd_action":"actionX"
            }
            """;

        EnvelopeResponse resp = mapper.readValue(json, EnvelopeResponse.class);

        assertThat(resp.getId())
            .isEqualTo(UUID.fromString("abcdefab-1234-5678-90ab-cdefabcdefab"));
        assertThat(resp.getCaseNumber()).isEqualTo("case123");
        assertThat(resp.getContainer()).isEqualTo("cont");
        assertThat(resp.getPoBox()).isEqualTo("PO123");
        assertThat(resp.getJurisdiction()).isEqualTo("J1");
        assertThat(resp.getDeliveryDate())
            .isEqualTo(Instant.parse("2025-01-02T00:00:00Z"));
        assertThat(resp.getOpeningDate())
            .isEqualTo(Instant.parse("2025-01-03T00:00:00Z"));
        assertThat(resp.getZipFileCreatedDate())
            .isEqualTo(Instant.parse("2025-01-04T00:00:00Z"));
        assertThat(resp.getZipFileName()).isEqualTo("zipname");
        assertThat(resp.getRescanFor()).isEqualTo("scan");
        assertThat(resp.getStatus()).isEqualTo(EnvelopeStatus.COMPLETED);
        assertThat(resp.getClassification()).isEqualTo("A");
        assertThat(resp.getScannableItems()).isEmpty();
        assertThat(resp.getPayments()).isEmpty();
        assertThat(resp.getNonScannableItems()).isEmpty();
        assertThat(resp.getCcdId()).isEqualTo("ccd111");
        assertThat(resp.getEnvelopeCcdAction()).isEqualTo("actionX");
    }
}
