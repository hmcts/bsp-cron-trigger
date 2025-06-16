package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import uk.gov.hmcts.reform.bsp.models.Payment;
import uk.gov.hmcts.reform.bsp.models.UpdatePayment;

import java.util.List;

@FeignClient(
    name = "bulkScanOrchestratorClient",
    url = "${url.bulk-scan-orchestrator}"
)
public interface BulkScanOrchestratorClient {

    @GetMapping("/payments/new/failed")
    List<Payment> getFailedNewPayments();

    @PutMapping("/payments/new/retry/{paymentId}")
    Payment retryNewPayment(@PathVariable("paymentId") String paymentId);

    @GetMapping("/payments/updated/failed")
    List<UpdatePayment> getFailedUpdatePayments();

    @PutMapping("/payments/updated/retry/{paymentId}")
    UpdatePayment retryUpdatedPayment(@PathVariable("paymentId") String paymentId);
}
