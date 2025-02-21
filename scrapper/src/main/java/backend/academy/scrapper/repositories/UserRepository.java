package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    @Query("select distinct u.id from User u join u.links tl where tl.serviceId = ?1")
    List<Long> findDistinctUserIdsWhereAnyLinkWithServiceId(String serviceId);
}
