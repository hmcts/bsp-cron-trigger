package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NonScannableItemResponseTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        mapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    @DisplayName("Lombok getters & setters should work")
    void lombokAccessors() {
        NonScannableItemResponse item = new NonScannableItemResponse();

        item.setDocumentControlNumber("DCN123");
        item.setItemType("BARCODE");

        assertThat(item.getDocumentControlNumber()).isEqualTo("DCN123");
        assertThat(item.getItemType()).isEqualTo("BARCODE");
    }

    @Test
    @DisplayName("Should serialize to JSON with snake_case field names")
    void serializeToJson() throws Exception {
        NonScannableItemResponse item = new NonScannableItemResponse();
        item.setDocumentControlNumber("DCN123");
        item.setItemType("BARCODE");

        String json = mapper.writeValueAsString(item);

        // both properties must appear with the correct JSON names
        assertThat(json)
            .contains("\"document_control_number\":\"DCN123\"")
            .contains("\"item_type\":\"BARCODE\"");
    }

    @Test
    @DisplayName("Should deserialize from JSON with snake_case field names")
    void deserializeFromJson() throws Exception {
        String json = """
            {
              "document_control_number": "DCN456",
              "item_type": "MANUAL"
            }
            """;

        NonScannableItemResponse item = mapper.readValue(json, NonScannableItemResponse.class);

        assertThat(item.getDocumentControlNumber()).isEqualTo("DCN456");
        assertThat(item.getItemType()).isEqualTo("MANUAL");
    }
}
