package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "bulkScanProcessorClient",
    url = "${url.bulk-scan-processor}"
)
public interface BulkScanProcessorClient {

    //    @GetMapping("/envelopes/stale-incomplete-envelopes")
    //    List<EnvelopeEntry> getStaleIncompleteEnvelopes();

    @DeleteMapping("/envelopes/stale/{envelopeId}")
    void deleteStaleEnvelope(@PathVariable("envelopeId") String envelopeId,
                             @RequestHeader("Authorization") String bearerToken);

    @PutMapping("/actions/reprocess/{envelopeId}")
    void reprocessEnvelope(@PathVariable("envelopeId") String envelopeId,
                           @RequestHeader("Authorization") String bearerToken);

    //    @GetMapping("/envelopes/{container}/{fileName}")
    //    EnvelopeEntry fetchEnvelopeDetails(@PathVariable("container") String container,
    //                                       @PathVariable("fileName") String fileName);
}
