package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

//@Repository
//@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "orm")
//@NoRepositoryBean
public interface LinkRepository {
    boolean existsByUserAndMonitoringServiceAndServiceId(User user, String monitoringService, String serviceId);

    TrackedLink save(TrackedLink link);

    void deleteById(long id);

    Page<TrackedLink> findAllByMonitoringServiceAndLastUpdateLessThanOrderById(
            String monitoring, long lastUpdate, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update TrackedLink set lastUpdate = ?1 where monitoringService = ?2 and serviceId in ?3")
    void updateAllByMonitoringServiceAndServiceIdIsIn(Long newLastUpdate, String monitoringService, List<String> sIds);
}
