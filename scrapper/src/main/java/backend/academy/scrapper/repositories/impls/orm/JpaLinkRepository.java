package backend.academy.scrapper.repositories.impls.orm;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.repositories.LinkRepository;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "orm")
public interface JpaLinkRepository extends LinkRepository, JpaRepository<TrackedLink, Long> {
    @Modifying
    @Transactional
    @Query("update TrackedLink set lastUpdate = ?1 where monitoringService = ?2 and serviceId in ?3")
    void updateAllByMonitoringServiceAndServiceIdIsIn(Long newLastUpdate, String monitoringService, List<String> sIds);
}
