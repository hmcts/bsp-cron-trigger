package uk.gov.hmcts.reform.bsp.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
    name = "sendLetterServiceClient",
    url = "${url.send-letter-service}"
)
public interface SendLetterServiceClient {

    //        @GetMapping("/stale-letters")
    //        List<LetterEntry> getStaleLetters();

    @PutMapping("/letters/{letterId}/mark-aborted")
    void markAborted(@PathVariable("letterId") String letterId,
                     @RequestHeader("Authorization") String bearerToken);

    @PutMapping("/letters/{letterId}/mark-created")
    void markCreated(@PathVariable("letterId") String letterId,
                     @RequestHeader("Authorization") String bearerToken);
}
