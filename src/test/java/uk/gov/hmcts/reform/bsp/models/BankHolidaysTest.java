package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class BankHolidaysTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    @Test
    void should_serialize_and_deserialize_correctly() throws Exception {
        BankHolidayEvent event = new BankHolidayEvent();
        event.date = "2026-03-23";

        RegionBankHolidays region = new RegionBankHolidays();
        region.events = Collections.singletonList(event);

        BankHolidays holidays = new BankHolidays();
        holidays.englandAndWales = region;

        String json = mapper.writeValueAsString(holidays);

        assertThat(json).contains("\"england-and-wales\"");
        assertThat(json).contains("\"date\":\"2026-03-23\"");

        BankHolidays deserialized = mapper.readValue(json, BankHolidays.class);

        assertThat(deserialized.englandAndWales).isNotNull();
        assertThat(deserialized.englandAndWales.events).hasSize(1);
        assertThat(deserialized.englandAndWales.events.get(0).date).isEqualTo("2026-03-23");
    }

    @Test
    void should_deserialize_from_gov_uk_format() throws Exception {
        String json = "{\"england-and-wales\":{\"events\":[{\"date\":\"2026-01-01\"}]}}";
        BankHolidays deserialized = mapper.readValue(json, BankHolidays.class);

        assertThat(deserialized.englandAndWales.events.get(0).date).isEqualTo("2026-01-01");
    }
}
