package backend.academy.scrapper.service;

import static org.mockito.Mockito.*;

import backend.academy.scrapper.service.monitoring.LinkDistributionService;
import backend.academy.scrapper.service.monitoring.LinkMonitor;
import backend.academy.scrapper.service.monitoring.Updates;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateSchedulerTest {
    @Mock
    private LinkDistributionService distributionService;

    @Mock
    private BotService botService;

    @InjectMocks
    private UpdateScheduler scheduler;

    @Test
    public void checkForUpdates_updatesNotFound_dontCallBotService() {
        LinkMonitor m1 = mock(LinkMonitor.class);
        when(m1.checkForUpdates()).thenReturn(new Updates());
        LinkMonitor m2 = mock(LinkMonitor.class);
        when(m2.checkForUpdates()).thenReturn(new Updates());
        when(distributionService.getMonitors()).thenReturn(List.of(m1, m2));

        scheduler.checkForUpdates();

        verify(botService, never()).sendUpdates(any());
    }

    @Test
    public void checkForUpdates_updatesFound_updatedPassedToBotService() {
        Updates u1 = new Updates();
        u1.addUpdate(new Updates.Update(List.of(1L), "A", "B"));
        u1.addUpdate(new Updates.Update(List.of(2L), "AA", "BB"));
        Updates u2 = new Updates();
        u2.addUpdate(new Updates.Update(List.of(3L), "AAA", "BBB"));
        u2.addUpdate(new Updates.Update(List.of(4L, 4L), "AAAA", "BBBB"));

        var expectedDetails = List.of(
                new Updates.Update(List.of(1L), "A", "B"),
                new Updates.Update(List.of(2L), "AA", "BB"),
                new Updates.Update(List.of(3L), "AAA", "BBB"),
                new Updates.Update(List.of(4L, 4L), "AAAA", "BBBB"));

        LinkMonitor m1 = mock(LinkMonitor.class);
        when(m1.checkForUpdates()).thenReturn(u1);
        LinkMonitor m2 = mock(LinkMonitor.class);
        when(m2.checkForUpdates()).thenReturn(u2);
        when(distributionService.getMonitors()).thenReturn(List.of(m1, m2));

        scheduler.checkForUpdates();

        ArgumentCaptor<Updates> captor = ArgumentCaptor.forClass(Updates.class);

        verify(botService).sendUpdates(captor.capture());

        var resultDetails = captor.getValue().getUpdates();

        Assertions.assertThatCollection(resultDetails).containsExactlyInAnyOrderElementsOf(expectedDetails);
    }
}
