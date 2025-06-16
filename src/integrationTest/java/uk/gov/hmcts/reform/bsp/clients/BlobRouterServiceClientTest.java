package uk.gov.hmcts.reform.bsp.clients;

import jdk.jfr.Description;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.bsp.config.feign.BlobRouterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackClient;
import uk.gov.hmcts.reform.bsp.models.SearchResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.enabled=true",
    "app.trigger-type=BULK_SCAN_CHECKS"
})
@Description("Check the integration between Blob Router works as expected. "
    + "Note that the abort and create endpoints are not checked here, as the service once deployed "
    + "Will be removing them all twice daily.")
class BlobRouterServiceClientTest {

    @Autowired
    private BlobRouterServiceClient client;

    @MockitoBean
    private SlackClient slackClient;

    @DisplayName("Should delete all stale blobs and return successfully")
    @Test
    void deleteAllStaleBlobsTest() {
        SearchResult<String> resp = client.deleteAllStaleBlobs(168);

        assertThat(resp).isNotNull();
        assertThat(resp.getData()).isNotNull();
        assertThat(resp.getCount())
            .withFailMessage("count must equal number of deleted blob IDs")
            .isEqualTo(resp.getData().size());
        resp.getData().forEach(idStr -> {
            UUID id = UUID.fromString(idStr);
            assertThat(id).isNotNull();
        });
    }
}
