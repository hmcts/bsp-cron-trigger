package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    name = "blobRouterServiceClient",
    url = "${url.blob-router-service}"
)
public interface BlobRouterServiceClient {

    @GetMapping("/health")
    String getHealth();
}
