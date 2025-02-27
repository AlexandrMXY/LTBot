package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.User;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("prod")
public interface UserRepository extends CrudRepository<User, Long> {
    @Query("select distinct u.id from User u join u.links tl where tl.serviceId = ?1")
    List<Long> findDistinctUserIdsWhereAnyLinkWithServiceId(String serviceId);
}
