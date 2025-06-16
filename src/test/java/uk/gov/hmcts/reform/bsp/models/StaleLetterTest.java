package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StaleLetterTest {

    @Test
    @DisplayName("Constructor and getters should assign fields correctly")
    void constructorAndGetters_workAsExpected() {
        UUID id = UUID.randomUUID();
        String status = "Pending";
        String service = "bulkPrint";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime then = now.minusHours(1);

        StaleLetter letter = new StaleLetter(id, status, service, then, now);

        assertThat(letter.getId()).isEqualTo(id);
        assertThat(letter.getStatus()).isEqualTo(status);
        assertThat(letter.getService()).isEqualTo(service);
        assertThat(letter.getCreatedAt()).isEqualTo(then);
        assertThat(letter.getSentToPrintAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("JSON deserialization should bind all properties")
    void jsonDeserialization_shouldBindFields() throws Exception {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String json = """
            {
              "id": "123e4567-e89b-12d3-a456-426614174000",
              "status": "Uploaded",
              "service": "bulkScan",
              "created_at": "2025-06-10T09:15:30",
              "sent_to_print_at": "2025-06-11T14:20:00"
            }
            """;

        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        StaleLetter letter = mapper.readValue(json, StaleLetter.class);

        assertThat(letter.getId()).isEqualTo(id);
        assertThat(letter.getStatus()).isEqualTo("Uploaded");
        assertThat(letter.getService()).isEqualTo("bulkScan");
        assertThat(letter.getCreatedAt())
            .isEqualTo(LocalDateTime.of(2025, 6, 10, 9, 15, 30));
        assertThat(letter.getSentToPrintAt())
            .isEqualTo(LocalDateTime.of(2025, 6, 11, 14, 20, 0));
    }
}
