package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.service.monitoring.LinkDistributionService;
import backend.academy.scrapper.service.monitoring.LinkMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UpdateScheduler {
    @Autowired
    private LinkDistributionService linkDistributionService;

    @Autowired
    private BotNotifierService botNotifierService;

    @Scheduled(fixedDelayString = "${app.update-delay}") // TODO
    public void checkForUpdates() {
        linkDistributionService.getMonitors().forEach(this::tryCheckForUpdates);

        log.atInfo().setMessage("Checking for updates complete").log();
    }

    private void tryCheckForUpdates(LinkMonitor monitor) {
        try {
            monitor.checkForUpdates(this::updatesConsumer);
        } catch (Exception e) {
            log.atWarn().setMessage("Checking for updates error").setCause(e).log();
        }
    }

    private void updatesConsumer(Updates updates) {
        botNotifierService.sendUpdates(updates);
    }
}
