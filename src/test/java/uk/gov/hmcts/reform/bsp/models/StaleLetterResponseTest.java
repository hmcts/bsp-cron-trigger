package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StaleLetterResponseTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper()
            // needed for LocalDateTime support
            .registerModule(new JavaTimeModule());
    }

    @DisplayName("Should deserialize StaleLetterResponse with nested StaleLetter list")
    @Test
    void jsonDeserialization_shouldBindFields() throws Exception {
        String isoNow       = LocalDateTime.now().toString();
        String isoWeekAgo   = LocalDateTime.now().minusDays(7).toString();
        UUID   uuid1        = UUID.randomUUID();
        UUID   uuid2        = UUID.randomUUID();

        String json = """
            {
              "count": 2,
              "stale_letters": [
                {
                  "id": "%s",
                  "status": "Pending",
                  "service": "BSProcessor",
                  "created_at": "%s",
                  "sent_to_print_at": "%s"
                },
                {
                  "id": "%s",
                  "status": "Uploaded",
                  "service": "BulkPrint",
                  "created_at": "%s",
                  "sent_to_print_at": "%s"
                }
              ]
            }
            """.formatted(
            uuid1, isoWeekAgo, isoNow,
            uuid2, isoWeekAgo, isoWeekAgo
        );

        StaleLetterResponse resp = mapper.readValue(json, StaleLetterResponse.class);

        // top-level
        assertThat(resp).isNotNull();
        assertThat(resp.getCount())
            .withFailMessage("count must be 2")
            .isEqualTo(2);
        assertThat(resp.getStaleLetters()).hasSize(2);

        // first letter
        StaleLetter first = resp.getStaleLetters().get(0);
        assertThat(first.getId()).isEqualTo(uuid1);
        assertThat(first.getStatus()).isEqualTo("Pending");
        assertThat(first.getService()).isEqualTo("BSProcessor");
        assertThat(first.getCreatedAt())
            .withFailMessage("parsed createdAt")
            .isEqualTo(LocalDateTime.parse(isoWeekAgo));
        assertThat(first.getSentToPrintAt())
            .withFailMessage("parsed sentToPrintAt")
            .isEqualTo(LocalDateTime.parse(isoNow));

        // second letter
        StaleLetter second = resp.getStaleLetters().get(1);
        assertThat(second.getId()).isEqualTo(uuid2);
        assertThat(second.getStatus()).isEqualTo("Uploaded");
        assertThat(second.getService()).isEqualTo("BulkPrint");
        assertThat(second.getCreatedAt())
            .isEqualTo(LocalDateTime.parse(isoWeekAgo));
        assertThat(second.getSentToPrintAt())
            .isEqualTo(LocalDateTime.parse(isoWeekAgo));
    }
}
