package backend.academy.scrapper.service.monitoring;

import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.service.monitoring.collectors.LinkUpdatesCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractMonitor implements LinkMonitor {
    private static final int PAGE_SIZE = 2;

    public final String MONITOR_NAME;
    protected final LinkUpdatesCollector linkUpdatesCollector;
    protected final LinkRepository linkRepository;

    @Override
    public void checkForUpdates(Consumer<Updates> updatesConsumer) {
        long updateBeginTime = System.currentTimeMillis() / 1000L;

        Pageable page = Pageable.ofSize(PAGE_SIZE);
        Page<TrackedLink> links;

        do {
            links = linkRepository.findAllByMonitoringServiceAndLastUpdateLessThanOrderById(
                MONITOR_NAME, updateBeginTime, page);

            try {
                long updateTime = System.currentTimeMillis() / 1000L;
                Updates updates = linkUpdatesCollector.getUpdates(links.stream());
                updatesConsumer.accept(updates);

                linkRepository.updateAllByMonitoringServiceAndServiceIdIsIn(
                    updateTime, MONITOR_NAME, links.map(TrackedLink::serviceId).stream().toList());
            } catch (Exception e) {
                log.atWarn()
                    .setMessage("Error during checking for updates")
                    .setCause(e)
                    .log();

            } finally {
                page = page.next();
            }
        } while (page.getPageNumber() > links.getTotalPages());
    }

}
