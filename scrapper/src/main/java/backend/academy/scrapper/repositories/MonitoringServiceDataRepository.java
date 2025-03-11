package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.MonitoringServiceData;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonitoringServiceDataRepository extends CrudRepository<MonitoringServiceData, String> {}
