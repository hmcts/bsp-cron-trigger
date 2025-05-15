package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    name = "sendLetterServiceClient",
    url = "${url.send-letter-service}"
)
public interface SendLetterServiceClient {

    @GetMapping("/health")
    String getHealth();
}
