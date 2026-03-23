package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmcts.reform.bsp.models.BankHolidays;

@FeignClient(name = "bankHolidayClient", url = "https://www.gov.uk")
public interface BankHolidayClient {
    @GetMapping("/bank-holidays.json")
    BankHolidays getBankHolidays();
}
