package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import java.util.stream.Stream;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends CrudRepository<TrackedLink, Long> {
    Stream<TrackedLink> findAllByMonitoringService(String monitoringService);

    boolean existsByUserAndMonitoringServiceAndServiceId(User user, String monitoringService, String serviceId);

}
