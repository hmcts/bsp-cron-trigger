package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentResponseTest {

    private final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    @DisplayName("JSON serialization should use annotated property names and format Instant correctly")
    void jsonSerialization_shouldIncludeAllFields() throws Exception {
        PaymentResponse response = new PaymentResponse();
        response.setId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        response.setStatus("COMPLETED");
        Instant now = Instant.parse("2025-06-16T10:00:00Z");
        response.setLastModified(now);
        response.setDocumentControlNumber("DCN123");

        String json = mapper.writeValueAsString(response);

        assertThat(json)
            .contains("\"id\":\"123e4567-e89b-12d3-a456-426614174000\"")
            .contains("\"status\":\"COMPLETED\"")
            .contains("\"last_modified\":\"2025-06-16T10:00:00Z\"")
            .contains("\"document_control_number\":\"DCN123\"");
    }

    @Test
    @DisplayName("JSON deserialization should populate all fields correctly, including Instant")
    void jsonDeserialization_shouldPopulateFields() throws Exception {
        String json = "{"
            + "\"id\":\"123e4567-e89b-12d3-a456-426614174000\","
            + "\"status\":\"FAILED\","
            + "\"last_modified\":\"2025-06-16T10:00:00Z\","
            + "\"document_control_number\":\"DCN456\""
            + "}";

        PaymentResponse response = mapper.readValue(json, PaymentResponse.class);

        assertThat(response.getId()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getLastModified()).isEqualTo(Instant.parse("2025-06-16T10:00:00Z"));
        assertThat(response.getDocumentControlNumber()).isEqualTo("DCN456");
    }
}
