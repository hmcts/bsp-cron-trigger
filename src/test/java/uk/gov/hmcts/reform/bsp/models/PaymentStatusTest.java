package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentStatusTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Enum contains exactly PENDING, COMPLETE, FAILED")
    void enumValues_shouldMatchExpected() {
        PaymentStatus[] values = PaymentStatus.values();
        assertThat(values)
            .containsExactly(PaymentStatus.PENDING, PaymentStatus.COMPLETE, PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Should serialize enum to its name")
    void jsonSerialization_shouldUseName() throws Exception {
        String json = mapper.writeValueAsString(PaymentStatus.COMPLETE);
        // JSON string for COMPLETE should be "\"COMPLETE\""
        assertThat(json).isEqualTo("\"COMPLETE\"");
    }

    @Test
    @DisplayName("Should deserialize name back into enum")
    void jsonDeserialization_shouldReturnEnum() throws Exception {
        String json = "\"FAILED\"";
        PaymentStatus status = mapper.readValue(json, PaymentStatus.class);
        assertThat(status).isEqualTo(PaymentStatus.FAILED);
    }
}
