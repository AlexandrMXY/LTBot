package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.TrackedLink;
import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Flux;
import java.util.List;

public interface LinkRepository extends CrudRepository<TrackedLink, Long> {
    List<TrackedLink> findAllByMonitoringService(String monitoringService);

    List<TrackedLink> findAllByMonitoringServiceAndServiceIdIn(String monitoringService, List<String> serviceId);
}
