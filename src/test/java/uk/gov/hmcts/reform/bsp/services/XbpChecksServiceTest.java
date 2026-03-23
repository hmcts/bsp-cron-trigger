package uk.gov.hmcts.reform.bsp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bsp.config.feign.BankHolidayClient;
import uk.gov.hmcts.reform.bsp.config.feign.BlobRouterServiceClient;
import uk.gov.hmcts.reform.bsp.integrations.SlackMessageHelper;
import uk.gov.hmcts.reform.bsp.models.BankHolidayEvent;
import uk.gov.hmcts.reform.bsp.models.BankHolidays;
import uk.gov.hmcts.reform.bsp.models.RegionBankHolidays;
import uk.gov.hmcts.reform.bsp.models.ReportSummaryResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XbpChecksServiceTest {

    @Mock
    private BlobRouterServiceClient blobClient;
    @Mock
    private BankHolidayClient bankHolidayClient;
    @Mock
    private SlackMessageHelper slackHelper;

    @InjectMocks
    private XbpChecksService service;

    @BeforeEach
    void setUp() {
        // By default, today is not a bank holiday
        BankHolidays nonHoliday = new BankHolidays();
        nonHoliday.englandAndWales = new RegionBankHolidays();
        nonHoliday.englandAndWales.events = Collections.emptyList();
        lenient().when(bankHolidayClient.getBankHolidays()).thenReturn(nonHoliday);
    }

    @Test
    void runChecks_whenFilesFound_reportsSuccess() {
        ReportSummaryResponse xbpFiles = new ReportSummaryResponse();
        xbpFiles.setTotalReceived(1);
        when(blobClient.getBlobReportsByDate(anyString())).thenReturn(xbpFiles);

        service.runChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());

        String actual = captor.getValue();
        assertAll(
            "xbp no-issues summary",
            () -> assertTrue(actual.contains("XBP Daily Check")),
            () -> assertTrue(actual.contains("All clear! No XBP issues detected"))
        );
    }

    @Test
    void runChecks_whenNoFilesFound_reportsXbpError() {
        ReportSummaryResponse xbpFiles = new ReportSummaryResponse();
        xbpFiles.setTotalReceived(0);
        when(blobClient.getBlobReportsByDate(anyString())).thenReturn(xbpFiles);

        service.runChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains("No files from XBP have come through for today."));
    }

    @Test
    void runChecks_whenCheckFails_reportsXbpCheckError() {
        when(blobClient.getBlobReportsByDate(anyString()))
            .thenThrow(new RuntimeException("xbp-fail"));

        service.runChecks();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains("Failed to check XBP files. Check App insights."));
    }

    @Test
    void runChecks_shouldSkip_onBankHoliday() {
        // Arrange
        String today = java.time.LocalDate.now().toString();
        BankHolidays holidays = new BankHolidays();
        holidays.englandAndWales = new RegionBankHolidays();
        BankHolidayEvent event = new BankHolidayEvent();
        event.date = today;
        holidays.englandAndWales.events = Collections.singletonList(event);

        when(bankHolidayClient.getBankHolidays()).thenReturn(holidays);

        // Act
        service.runChecks();

        // Assert
        verify(blobClient, never()).getBlobReportsByDate(anyString());

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slackHelper).sendLongMessage(captor.capture());
        assertTrue(captor.getValue().contains("All clear! No XBP issues detected."));
    }
}
