package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Lombok getters & setters should work")
    void lombokAccessors() {
        Payment payment = new Payment();

        UUID id = UUID.randomUUID();
        payment.setId(id);
        payment.setEnvelopeId("env123");
        payment.setCcdReference("ccd456");
        payment.setExceptionRecord(true);
        payment.setPoBox("PO1");
        payment.setJurisdiction("J1");
        payment.setService("S1");
        payment.setStatusMessage("OK");
        payment.setPayments(Arrays.asList("p1", "p2"));
        payment.setStatus(PaymentStatus.COMPLETE);
        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedAt(now.minusDays(1));
        payment.setLastUpdatedAt(now);

        assertThat(payment.getId()).isEqualTo(id);
        assertThat(payment.getEnvelopeId()).isEqualTo("env123");
        assertThat(payment.getCcdReference()).isEqualTo("ccd456");
        assertThat(payment.isExceptionRecord()).isTrue();
        assertThat(payment.getPoBox()).isEqualTo("PO1");
        assertThat(payment.getJurisdiction()).isEqualTo("J1");
        assertThat(payment.getService()).isEqualTo("S1");
        assertThat(payment.getStatusMessage()).isEqualTo("OK");
        assertThat(payment.getPayments()).containsExactly("p1", "p2");
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETE);
        assertThat(payment.getCreatedAt()).isBefore(payment.getLastUpdatedAt());
    }

    @Test
    @DisplayName("Should serialize to JSON with correct property names")
    void serializeToJson() throws Exception {
        Payment payment = new Payment();
        payment.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        payment.setEnvelopeId("env123");
        payment.setExceptionRecord(false);

        String json = mapper.writeValueAsString(payment);

        assertThat(json)
            .contains("\"id\":\"00000000-0000-0000-0000-000000000001\"")
            .contains("\"envelopeId\":\"env123\"")
            .contains("\"exceptionRecord\":false");
    }

    @Test
    @DisplayName("Should deserialize from JSON correctly")
    void deserializeFromJson() throws Exception {
        String json = "{"
            + "\"id\":\"00000000-0000-0000-0000-000000000002\"," +
            "\"envelopeId\":\"env456\"," +
            "\"exceptionRecord\":true," +
            "\"createdAt\":\"2025-06-15T10:00:00\"," +
            "\"lastUpdatedAt\":\"2025-06-16T12:00:00\"" +
            "}";

        Payment payment = mapper.readValue(json, Payment.class);

        assertThat(payment.getId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        assertThat(payment.getEnvelopeId()).isEqualTo("env456");
        assertThat(payment.isExceptionRecord()).isTrue();
        assertThat(payment.getCreatedAt()).isEqualTo(LocalDateTime.parse("2025-06-15T10:00:00"));
        assertThat(payment.getLastUpdatedAt()).isEqualTo(LocalDateTime.parse("2025-06-16T12:00:00"));
    }
}
