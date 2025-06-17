package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
}
