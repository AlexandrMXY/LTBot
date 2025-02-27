package backend.academy.scrapper.service;

import backend.academy.scrapper.service.monitoring.LinkDistributionService;
import backend.academy.scrapper.service.monitoring.LinkMonitor;
import backend.academy.scrapper.service.monitoring.Updates;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class UpdateScheduler {
    @Autowired
    private LinkDistributionService linkDistributionService;

    @Autowired
    private BotService botService;

    @Scheduled(fixedDelayString = "${app.update-delay}")
    public void checkForUpdates() {
        var result = linkDistributionService.getMonitors().stream()
                .map(LinkMonitor::checkForUpdates)
                .reduce(Updates::mergeResult);
        log.info("Checking for updates complete: {}", result);

        if (result.isPresent() && result.orElseThrow().hasUpdates()) {
            botService.sendUpdates(result.orElseThrow());
        }
    }
}
