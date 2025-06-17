package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeStatusTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    @DisplayName("Should deserialize each Status constant from JSON string")
    @Test
    void jsonDeserialization_allConstants() throws Exception {
        for (EnvelopeStatus s : EnvelopeStatus.values()) {
            String json = "\"" + s.name() + "\"";
            EnvelopeStatus parsed = mapper.readValue(json, EnvelopeStatus.class);
            assertThat(parsed).isEqualTo(s);
        }
    }

    @DisplayName("Should serialize each Status constant to its name")
    @Test
    void jsonSerialization_allConstants() throws Exception {
        for (EnvelopeStatus s : EnvelopeStatus.values()) {
            String json = mapper.writeValueAsString(s);
            assertThat(json).isEqualTo("\"" + s.name() + "\"");
        }
    }
}
