package backend.academy.scrapper.service.monitoring;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// TODO so ignores since update date
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractMonitor implements LinkMonitor {
    private static final int BATCH_SIZE = 1;
    private static final int BATCHES_CNT = 2;
    private static final int PAGE_SIZE = BATCH_SIZE * BATCHES_CNT;

    public final String MONITOR_NAME;
    protected final LinkUpdatesCollector linkUpdatesCollector;
    protected final LinkRepository linkRepository;

    @Override
    public void checkForUpdates(Consumer<Updates> updatesConsumer) {
        long updateBeginTime = System.currentTimeMillis() / 1000L;

        Pageable page = Pageable.ofSize(PAGE_SIZE);
        Page<TrackedLink> links;

        try (ExecutorService executorService = Executors.newFixedThreadPool(BATCHES_CNT)) {
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
        for (int i = 0; i < links.size(); i += BATCH_SIZE) {
            result.add(new LinksProcessor(links.subList(i, Math.min(i + BATCH_SIZE, links.size())), updatesConsumer));
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
