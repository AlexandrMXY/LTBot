package backend.academy.scrapper.service.monitoring.collectors;

import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.entities.TrackedLink;
import java.util.stream.Stream;

public interface LinkUpdatesCollector {
    Updates getUpdates(Stream<TrackedLink> links);
}
