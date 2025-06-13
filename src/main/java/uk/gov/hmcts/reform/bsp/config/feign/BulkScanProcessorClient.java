package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    name = "blobRouterServiceClient",
    url = "${url.bulk-scan-processor}"
)
public interface BulkScanProcessorClient {

    @GetMapping("/health")
    String getHealth();
}
