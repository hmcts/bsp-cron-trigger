package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "blobRouterServiceClient",
    url = "${url.blob-router-service}"
)
public interface BlobRouterServiceClient {

    @DeleteMapping("/envelopes/stale/all")
    String deleteAllStaleBlobs(@RequestHeader("Authorization") String bearerToken);
}
