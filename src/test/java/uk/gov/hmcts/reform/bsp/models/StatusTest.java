package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class StatusTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    @DisplayName("Should deserialize each Status constant from JSON string")
    @Test
    void jsonDeserialization_allConstants() throws Exception {
        for (Status s : Status.values()) {
            String json = "\"" + s.name() + "\"";
            Status parsed = mapper.readValue(json, Status.class);
            assertThat(parsed).isEqualTo(s);
        }
    }

    @DisplayName("Should serialize each Status constant to its name")
    @Test
    void jsonSerialization_allConstants() throws Exception {
        for (Status s : Status.values()) {
            String json = mapper.writeValueAsString(s);
            assertThat(json).isEqualTo("\"" + s.name() + "\"");
        }
    }

    @DisplayName("Unknown value should fail to deserialize")
    @Test
    void jsonDeserialization_unknownFails() {
        assertThatThrownBy(() -> mapper.readValue("\"UNKNOWN_STATUS\"", Status.class))
            .isInstanceOf(com.fasterxml.jackson.databind.exc.InvalidFormatException.class)
            .hasMessageContaining("Cannot deserialize value of type `uk.gov.hmcts.reform.bsp.models.Status`");
    }
}
