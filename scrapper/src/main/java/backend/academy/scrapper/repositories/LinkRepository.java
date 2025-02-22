package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Flux;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface LinkRepository extends CrudRepository<TrackedLink, Long> {
    Stream<TrackedLink> findAllByMonitoringService(String monitoringService);

    Stream<TrackedLink> findAllByMonitoringServiceAndServiceIdIn(String monitoringService, Collection<String> serviceId);

    boolean existsByUserAndMonitoringServiceAndServiceId(User user, String monitoringService, String serviceId);
}
