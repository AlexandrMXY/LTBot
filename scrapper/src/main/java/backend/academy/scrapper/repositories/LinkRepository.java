package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LinkRepository {
    boolean existsByUserAndMonitoringServiceAndServiceId(User user, String monitoringService, String serviceId);

    TrackedLink save(TrackedLink link);

    void deleteById(long id);

    Page<TrackedLink> findAllByMonitoringServiceAndLastUpdateLessThanOrderById(
            String monitoring, long lastUpdate, Pageable pageable);

    void updateAllByMonitoringServiceAndServiceIdIsIn(Long newLastUpdate, String monitoringService, List<String> sIds);

    List<TrackedLink> findAllByUserId(long userId);

    Optional<TrackedLink> findByUserIdAndUrl(long userId, String url);
}
