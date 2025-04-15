package backend.academy.scrapper.repositories.impls.orm;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.repositories.LinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "orm")
public interface JpaLinkRepository extends LinkRepository, JpaRepository<TrackedLink, Long> {
}
