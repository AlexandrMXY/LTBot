package backend.academy.scrapper.repositories.impls.orm;

import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "orm")
public interface JpaUserRepository extends UserRepository, JpaRepository<User, Long> {
}
