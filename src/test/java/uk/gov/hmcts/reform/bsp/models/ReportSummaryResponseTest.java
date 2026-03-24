package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ReportSummaryResponseTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    @Test
    void should_serialize_and_deserialize_correctly() throws Exception {
        ReportSummary item = new ReportSummary();
        item.setContainer("test-container");
        item.setReceived(10);
        item.setRejected(2);
        item.setDate("2026-03-23");

        ReportSummaryResponse response = new ReportSummaryResponse();
        response.setData(Collections.singletonList(item));
        response.setTotalReceived(10);
        response.setTotalRejected(2);
        response.setTimeStamp("2026-03-23 13:59:32");

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"total_received\":10");
        assertThat(json).contains("\"total_rejected\":2");
        assertThat(json).contains("\"time_stamp\":\"2026-03-23 13:59:32\"");

        ReportSummaryResponse deserialized = mapper.readValue(json, ReportSummaryResponse.class);

        assertThat(deserialized.getTotalReceived()).isEqualTo(10);
        assertThat(deserialized.getTotalRejected()).isEqualTo(2);
        assertThat(deserialized.getTimeStamp()).isEqualTo("2026-03-23 13:59:32");
        assertThat(deserialized.getData()).hasSize(1);
        assertThat(deserialized.getData().get(0).getContainer()).isEqualTo("test-container");
        assertThat(deserialized.getData().get(0).getReceived()).isEqualTo(10);
        assertThat(deserialized.getData().get(0).getRejected()).isEqualTo(2);
        assertThat(deserialized.getData().get(0).getDate()).isEqualTo("2026-03-23");
    }

    @Test
    void should_handle_null_data() throws Exception {
        String json = "{\"total_received\":0,\"total_rejected\":0,\"time_stamp\":\"2026-03-23 13:59:32\"}";
        ReportSummaryResponse deserialized = mapper.readValue(json, ReportSummaryResponse.class);

        assertThat(deserialized.getData()).isNull();
        assertThat(deserialized.getTotalReceived()).isZero();
    }
}
