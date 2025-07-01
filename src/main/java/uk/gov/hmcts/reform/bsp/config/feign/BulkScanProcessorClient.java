package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.bsp.models.EnvelopeInfo;
import uk.gov.hmcts.reform.bsp.models.EnvelopeResponse;
import uk.gov.hmcts.reform.bsp.models.SearchResult;

import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(
    name = "bulkScanProcessorClient",
    url = "${url.bulk-scan-processor}"
)
public interface BulkScanProcessorClient {

    @GetMapping("/envelopes/stale-incomplete-envelopes")
    SearchResult<EnvelopeInfo> getStaleIncompleteEnvelopes();

    @GetMapping("/envelopes/{container}/{file_name}")
    EnvelopeResponse fetchEnvelopeDetails(
        @PathVariable("container") String container,
        @PathVariable("file_name") String fileName
    );

    @PutMapping("/actions/reprocess/{envelopeId}")
    void reprocessEnvelope(
        @RequestHeader(value = AUTHORIZATION) String bearerToken,
        @PathVariable("envelopeId") UUID id
    );
}
