package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeInfoTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper()
            // write Instants as ISO strings
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Lombok getters/setters work")
    void lombokAccessors() {
        EnvelopeInfo info = new EnvelopeInfo();

        UUID id = UUID.randomUUID();
        Instant now = Instant.parse("2025-01-02T15:30:00Z");

        info.setContainer("my-container");
        info.setFileName("file.pdf");
        info.setEnvelopeId(id);
        info.setCreatedAt(now);

        assertThat(info.getContainer()).isEqualTo("my-container");
        assertThat(info.getFileName()).isEqualTo("file.pdf");
        assertThat(info.getEnvelopeId()).isEqualTo(id);
        assertThat(info.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Serializes to JSON with correct field names")
    void shouldSerializeToExpectedJson() throws Exception {
        EnvelopeInfo info = new EnvelopeInfo();
        info.setContainer("foo");
        info.setFileName("bar.pdf");
        info.setEnvelopeId(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        info.setCreatedAt(Instant.parse("2025-01-02T15:30:00Z"));

        String json = mapper.writeValueAsString(info);

        // quick string-based check for the exact field names
        assertThat(json)
            .contains("\"container\":\"foo\"")
            .contains("\"file_name\":\"bar.pdf\"")
            .contains("\"envelope_id\":\"123e4567-e89b-12d3-a456-426614174000\"")
            .contains("\"created_at\":\"2025-01-02T15:30:00Z\"");
    }

    @Test
    @DisplayName("Deserializes from JSON into all fields")
    void shouldDeserializeFromJson() throws Exception {
        String json = """
            {
              "container":"xyz",
              "file_name":"doc.jpg",
              "envelope_id":"abcdefab-1234-5678-90ab-cdefabcdefab",
              "created_at":"2025-12-31T23:59:59Z"
            }
            """;

        EnvelopeInfo info = mapper.readValue(json, EnvelopeInfo.class);

        assertThat(info.getContainer()).isEqualTo("xyz");
        assertThat(info.getFileName()).isEqualTo("doc.jpg");
        assertThat(info.getEnvelopeId())
            .isEqualTo(UUID.fromString("abcdefab-1234-5678-90ab-cdefabcdefab"));
        assertThat(info.getCreatedAt())
            .isEqualTo(Instant.parse("2025-12-31T23:59:59Z"));
    }
}
