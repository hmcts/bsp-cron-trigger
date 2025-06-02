package uk.gov.hmcts.reform.bsp.integrations;

import com.slack.api.RequestConfigurator;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.config.SlackProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlackClientTest {

    private final String token   = "xoxb-fake-token";
    private final String channel = "C12345";

    @Mock SlackProperties                    properties;
    @Mock MethodsClient                      methodsClient;
    @Mock ChatPostMessageResponse            okResponse;
    @Captor
    @SuppressWarnings("unchecked")
    private ArgumentCaptor<
        RequestConfigurator<
            ChatPostMessageRequest.ChatPostMessageRequestBuilder
            >
        > configuratorCaptor;

    @SuppressWarnings("unchecked")
    private <T> RequestConfigurator<T> anyConfigurator() {
        return (RequestConfigurator<T>) any(RequestConfigurator.class);
    }

    @Test
    void sendSlackMessage_shouldSendCorrectPayload_whenOk() throws Exception {
        // only stubbing what this test uses:
        when(properties.getTokenDailyChecks()).thenReturn(token);
        when(properties.getChannelIdDailyChecks()).thenReturn(channel);
        when(okResponse.isOk()).thenReturn(true);

        SlackClient client = new SlackClient(properties);

        try (MockedStatic<Slack> slackStatic = mockStatic(Slack.class)) {
            Slack slackMock = mock(Slack.class);
            slackStatic.when(Slack::getInstance).thenReturn(slackMock);
            when(slackMock.methods(token)).thenReturn(methodsClient);
            when(methodsClient.chatPostMessage(anyConfigurator()))
                .thenReturn(okResponse);

            client.sendSlackMessage("Hello, world!");

            verify(methodsClient).chatPostMessage(configuratorCaptor.capture());
            var configurator = configuratorCaptor.getValue();
            var builder      = ChatPostMessageRequest.builder();
            configurator.configure(builder);
            ChatPostMessageRequest req = builder.build();

            assertEquals(channel, req.getChannel());
            assertEquals("Hello, world!", req.getText());
        }
    }
}
