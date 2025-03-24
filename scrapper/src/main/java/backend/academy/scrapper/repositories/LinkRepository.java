package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app", name = "db-access-impl", havingValue = "orm")
public interface LinkRepository extends org.springframework.data.repository.Repository<TrackedLink, Long> {
    boolean existsByUserAndMonitoringServiceAndServiceId(User user, String monitoringService, String serviceId);

    TrackedLink save(TrackedLink link);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteById(long id);

    Page<TrackedLink> findAllByMonitoringServiceAndLastUpdateLessThanOrderById(String monitoring, long lastUpdate, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update TrackedLink set lastUpdate = ?1 where monitoringService = ?2 and serviceId in (?3)")
    void updateAllByMonitoringServiceAndServiceIdIsIn(Long newLastUpdate, String monitoringService, List<String> sIds);
}

