package uk.gov.hmcts.reform.bsp.integrations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SlackMessageHelperTest {

    private SlackClient slackClient;
    private SlackMessageHelper helper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        slackClient = mock(SlackClient.class);
        helper = new SlackMessageHelper(slackClient);
    }

    @Test
    void sendLongMessage_empty_doesNothing() {
        helper.sendLongMessage("");
        verifyNoInteractions(slackClient);
    }

    @Test
    void sendLongMessage_shortMessage_sendsOnce() {
        String msg = "Hello, Slack!";
        helper.sendLongMessage(msg);

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(slackClient, times(1)).sendSlackMessage(cap.capture());
        assertEquals(msg, cap.getValue());
    }

    @Test
    void sendLongMessage_exactChunk_sendsOneChunk() {
        String chunk = IntStream.range(0, SlackMessageHelper.MAX_CHUNK)
            .mapToObj(i -> "x")
            .collect(Collectors.joining());
        helper.sendLongMessage(chunk);

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(slackClient, times(1)).sendSlackMessage(cap.capture());
        assertEquals(SlackMessageHelper.MAX_CHUNK, cap.getValue().length());
        assertEquals(chunk, cap.getValue());
    }

    @Test
    void sendLongMessage_multipleChunks_sendsAllChunks() {
        int total = SlackMessageHelper.MAX_CHUNK * 2 + 123;
        String msg = IntStream.range(0, total)
            .mapToObj(i -> "a")
            .collect(Collectors.joining());

        helper.sendLongMessage(msg);

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(slackClient, times(3)).sendSlackMessage(cap.capture());

        List<String> chunks = cap.getAllValues();
        assertEquals(3, chunks.size());
        assertEquals(SlackMessageHelper.MAX_CHUNK, chunks.get(0).length());
        assertEquals(SlackMessageHelper.MAX_CHUNK, chunks.get(1).length());
        assertEquals(123, chunks.get(2).length());

        String reconstructed = String.join("", chunks);
        assertEquals(msg, reconstructed);
    }

    @Test
    void sendLongMessage_atTotalLimit_sendsExactlyTotal_Chunks() {
        int total = SlackMessageHelper.MAX_TOTAL;
        String msg = IntStream.range(0, total)
            .mapToObj(i -> "b")
            .collect(Collectors.joining());

        helper.sendLongMessage(msg);

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(slackClient, times(5)).sendSlackMessage(cap.capture());

        cap.getAllValues().forEach(chunk ->
                                       assertEquals(SlackMessageHelper.MAX_CHUNK, chunk.length())
        );
    }

    @Test
    void sendLongMessage_aboveTotalLimit_sendsWarningOnly() {
        int tooBig = SlackMessageHelper.MAX_TOTAL + 1;
        String msg = IntStream.range(0, tooBig)
            .mapToObj(i -> "z")
            .collect(Collectors.joining());

        helper.sendLongMessage(msg);

        ArgumentCaptor<String> cap = ArgumentCaptor.forClass(String.class);
        verify(slackClient, times(1)).sendSlackMessage(cap.capture());

        String sent = cap.getValue();
        assertTrue(sent.contains("warning"), "Should contain ‘warning’");
        assertTrue(sent.contains(String.valueOf(tooBig)), "Should report actual length");
        assertTrue(sent.contains(String.valueOf(SlackMessageHelper.MAX_TOTAL)), "Should report the limit");
    }
}
