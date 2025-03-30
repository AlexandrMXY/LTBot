package backend.academy.scrapper.service.monitoring;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.service.monitoring.collectors.LinkUpdatesCollector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Slf4j
public abstract class AbstractMonitor implements LinkMonitor {
    private final int batchSize;
    private final int batchesCnt;
    private final int pageSize;

    public final String MONITOR_NAME;
    protected final LinkUpdatesCollector linkUpdatesCollector;
    protected final LinkRepository linkRepository;

    public AbstractMonitor(
            String MONITOR_NAME,
            LinkUpdatesCollector linkUpdatesCollector,
            LinkRepository linkRepository,
            ScrapperConfig config) {
        batchSize = config.updateThreadBatchSize();
        batchesCnt = config.updateThreadsCnt();
        pageSize = batchSize * batchesCnt;
        this.MONITOR_NAME = MONITOR_NAME;
        this.linkUpdatesCollector = linkUpdatesCollector;
        this.linkRepository = linkRepository;
    }

    @Override
    public void checkForUpdates(Consumer<Updates> updatesConsumer) {
        long updateBeginTime = System.currentTimeMillis() / 1000L;

        Pageable page = Pageable.ofSize(pageSize);
        Page<TrackedLink> links;

        try (ExecutorService executorService = Executors.newFixedThreadPool(batchesCnt)) {
            do {
                links = linkRepository.findAllByMonitoringServiceAndLastUpdateLessThanOrderById(
                        MONITOR_NAME, updateBeginTime, page);
                List<TrackedLink> linksList = links.toList();

                if (links.isEmpty()) break;

                try {
                    long updateTime = System.currentTimeMillis() / 1000L;

                    var futures = executorService.invokeAll(createCallables(linksList, updatesConsumer));
                    for (var future : futures) future.get();

                    linkRepository.updateAllByMonitoringServiceAndServiceIdIsIn(
                            updateTime,
                            MONITOR_NAME,
                            links.map(TrackedLink::serviceId).toList());

                    page = page.withPage(0);
                } catch (Exception e) {
                    log.atWarn()
                            .setMessage("Error during checking for updates")
                            .setCause(e)
                            .log();
                    page = page.next();
                }
            } while (page.getPageNumber() < links.getTotalPages());
        } catch (Exception e) {
            log.atWarn()
                    .setMessage("Error during checking for updates")
                    .setCause(e)
                    .log();
        }
    }

    private List<Callable<Void>> createCallables(List<TrackedLink> links, Consumer<Updates> updatesConsumer) {
        List<Callable<Void>> result = new ArrayList<>();
        for (int i = 0; i < links.size(); i += batchSize) {
            result.add(new LinksProcessor(links.subList(i, Math.min(i + batchSize, links.size())), updatesConsumer));
        }
        return result;
    }

    @AllArgsConstructor
    private class LinksProcessor implements Callable<Void> {
        List<TrackedLink> links;
        Consumer<Updates> updatesConsumer;

        public Void call() {
            Updates updates = linkUpdatesCollector.getUpdates(links.stream().filter(this::checkIfLinkActive));
            updatesConsumer.accept(updates);

            return null;
        }

        private boolean checkIfLinkActive(TrackedLink link) {
            return Collections.disjoint(link.tags(), link.user().inactiveTags());
        }
    }
}
