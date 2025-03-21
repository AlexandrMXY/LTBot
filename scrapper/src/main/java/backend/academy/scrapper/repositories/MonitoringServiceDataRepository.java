package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.MonitoringServiceData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@ConditionalOnProperty(prefix = "app", name = "db-access-impl", havingValue = "orm")
public interface MonitoringServiceDataRepository extends org.springframework.data.repository.Repository<MonitoringServiceData, String> {
    MonitoringServiceData save(MonitoringServiceData data);

    Optional<MonitoringServiceData> findById(String id);

    boolean existsById(String id);
}
