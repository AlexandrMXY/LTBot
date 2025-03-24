//package backend.academy.scrapper.repositories.impls.sql;
//
//
//import backend.academy.scrapper.entities.User;
//import backend.academy.scrapper.repositories.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//@ConditionalOnProperty(prefix = "app", name = "db-access-impl", havingValue = "sql")
//public class SqlUserRepository implements UserRepository {
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Override
//    public List<Long> findDistinctUserIdsWhereAnyLinkWithServiceId(String serviceId) {
//        return null;
//    }
//
//    @Override
//    public User save(User user) {
//        return null;
//    }
//
//    @Override
//    public boolean existsById(long id) {
//        return false;
//    }
//
//    @Override
//    public void deleteById(long id) {
//
//    }
//
//    @Override
//    public Optional<User> findById(long id) {
//        return Optional.empty();
//    }
//}
