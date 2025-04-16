package backend.academy.scrapper.repositories;

import backend.academy.scrapper.entities.User;
import java.util.Optional;

// @Repository
// @ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "orm")
// @NoRepositoryBean
public interface UserRepository {
    User save(User user);

    boolean existsById(long id);

    void deleteById(long id);

    Optional<User> findById(long id);
}
