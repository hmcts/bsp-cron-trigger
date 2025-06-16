package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UpdatePaymentTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void init() {
        mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    }

    @DisplayName("Should deserialize JSON into UpdatePayment")
    @Test
    void jsonDeserialization_shouldBindAllFields() throws Exception {
        UUID id = UUID.randomUUID();
        String envelopeId = "env-123";
        String jurisdiction = "IA";
        String exceptionRecordRef = "ex-456";
        String newCaseRef = "new-789";
        String statusMessage = "Something went wrong";
        String status = "FAILED";
        String createdAt = "2025-06-16T10:30:00";
        String lastUpdatedAt = "2025-06-16T11:45:30";

        String json = """
            {
              "id":"%s",
              "envelopeId":"%s",
              "jurisdiction":"%s",
              "exceptionRecordRef":"%s",
              "newCaseRef":"%s",
              "statusMessage":"%s",
              "status":"%s",
              "createdAt":"%s",
              "lastUpdatedAt":"%s"
            }
            """.formatted(
            id, envelopeId, jurisdiction, exceptionRecordRef,
            newCaseRef, statusMessage, status, createdAt, lastUpdatedAt
        );

        UpdatePayment up = mapper.readValue(json, UpdatePayment.class);

        assertThat(up.getId()).isEqualTo(id);
        assertThat(up.getEnvelopeId()).isEqualTo(envelopeId);
        assertThat(up.getJurisdiction()).isEqualTo(jurisdiction);
        assertThat(up.getExceptionRecordRef()).isEqualTo(exceptionRecordRef);
        assertThat(up.getNewCaseRef()).isEqualTo(newCaseRef);
        assertThat(up.getStatusMessage()).isEqualTo(statusMessage);
        assertThat(up.getStatus()).isEqualTo(PaymentStatus.valueOf(status));
        assertThat(up.getCreatedAt()).isEqualTo(LocalDateTime.parse(createdAt));
        assertThat(up.getLastUpdatedAt()).isEqualTo(LocalDateTime.parse(lastUpdatedAt));
    }

    @DisplayName("Should serialize UpdatePayment back to JSON")
    @Test
    void jsonSerialization_shouldIncludeAllFields() throws Exception {
        UpdatePayment up = new UpdatePayment();
        UUID id = UUID.randomUUID();
        up.setId(id);
        up.setEnvelopeId("env-abc");
        up.setJurisdiction("IA");
        up.setExceptionRecordRef("ex-def");
        up.setNewCaseRef("new-ghi");
        up.setStatusMessage("OK");
        up.setStatus(PaymentStatus.COMPLETE);
        LocalDateTime now = LocalDateTime.now().withNano(0);
        up.setCreatedAt(now);
        up.setLastUpdatedAt(now);

        String serialized = mapper.writeValueAsString(up);

        UpdatePayment round = mapper.readValue(serialized, UpdatePayment.class);
        assertThat(round).usingRecursiveComparison().isEqualTo(up);
    }
}
