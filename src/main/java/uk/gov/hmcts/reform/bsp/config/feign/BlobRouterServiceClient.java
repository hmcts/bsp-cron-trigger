package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.bsp.models.ReportSummaryResponse;
import uk.gov.hmcts.reform.bsp.models.SearchResult;

@FeignClient(
    name = "blobRouterServiceClient",
    url = "${url.blob-router-service}"
)
public interface BlobRouterServiceClient {

    @DeleteMapping("/envelopes/stale/all")
    SearchResult<String> deleteAllStaleBlobs(
        @RequestParam(name = "stale_time", required = false, defaultValue = "168")
        int staleTime
    );

    @GetMapping("/reports/count-summary")
    ReportSummaryResponse getBlobReportsByDate(
        @RequestParam(name = "date", required = true, defaultValue = "2026-03-01")
        String date
    );
}
