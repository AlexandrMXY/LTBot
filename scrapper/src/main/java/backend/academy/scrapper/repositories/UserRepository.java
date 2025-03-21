package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.User;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "db-access-impl", havingValue = "orm")
public interface UserRepository extends org.springframework.data.repository.Repository<User, Long> {
    @Query("select distinct u.id from User u join u.links tl where tl.serviceId = ?1")
    List<Long> findDistinctUserIdsWhereAnyLinkWithServiceId(String serviceId);

    User save(User user);

    boolean existsById(long id);

    void deleteById(long id);

    Optional<User> findById(long id);
}
