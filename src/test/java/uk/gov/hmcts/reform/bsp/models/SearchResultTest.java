package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SearchResultTest {

    @Test
    @DisplayName("Getter and setter should work correctly")
    void gettersAndSetters_workAsExpected() {
        SearchResult<String> sr = new SearchResult<>();
        sr.setCount(2);
        sr.setData(List.of("first", "second"));

        assertThat(sr.getCount()).isEqualTo(2);
        assertThat(sr.getData()).containsExactly("first", "second");
    }

    @Test
    @DisplayName("Should deserialize JSON into SearchResult<String>")
    void jsonDeserialize_shouldMapCountAndData() throws Exception {
        String json = """
            {
              "count": 3,
              "data": ["mosh", "kupo", "cats"]
            }
            """;

        ObjectMapper mapper = new ObjectMapper();
        SearchResult<String> sr = mapper.readValue(
            json,
            new TypeReference<>() {
            }
        );

        assertThat(sr.getCount())
            .as("count field")
            .isEqualTo(3);

        assertThat(sr.getData())
            .as("data list")
            .containsExactly("mosh", "kupo", "cats");
    }
}
