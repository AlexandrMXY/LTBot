package backend.academy.scrapper.service;

import backend.academy.scrapper.service.monitoring.LinkDistributionService;
import backend.academy.scrapper.service.monitoring.LinkMonitor;
import backend.academy.scrapper.service.monitoring.Updates;
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
    private BotService botService;

    @Scheduled(fixedDelayString = "${app.update-delay}")
    public void checkForUpdates() {
        var result = linkDistributionService.getMonitors().stream()
                .map(this::tryCheckForUpdates)
                .reduce(Updates::mergeResult);

        log.atInfo()
                .setMessage("Checking for updates complete")
                .addKeyValue(
                        "updates_found",
                        result.orElse(new Updates()).getUpdates().size())
                .log();

        if (result.isPresent() && result.orElseThrow().hasUpdates()) {
            botService.sendUpdates(result.orElseThrow());
        }
    }

    private Updates tryCheckForUpdates(LinkMonitor monitor) {
        try {
            return monitor.checkForUpdates();
        } catch (Exception e) {
            log.atWarn().setMessage("Checking for updates error").setCause(e).log();
            return new Updates();
        }
    }
}
