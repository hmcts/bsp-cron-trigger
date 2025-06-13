package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(
    name = "bulkScanOrchestratorClient",
    url = "${url.bulk-scan-orchestrator}"
)
public interface BulkScanOrchestratorClient {

    //    @GetMapping("/payments/updated/failed")
    //    List<PaymentEntry> getFailedPayments();

    @PutMapping("/payments/updated/retry/{paymentId}")
    void retryPayment(@PathVariable("paymentId") String paymentId);
}
