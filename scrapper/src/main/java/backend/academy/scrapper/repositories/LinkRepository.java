package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.TrackedLink;
import org.springframework.data.repository.CrudRepository;

public interface LinkRepository extends CrudRepository<TrackedLink, Long> {
}
