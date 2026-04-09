package uk.gov.hmcts.reform.bsp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankHolidays {
    @JsonProperty("england-and-wales")
    public RegionBankHolidays englandAndWales;
}

