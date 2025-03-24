package backend.academy.scrapper.repositories.impls.sql;


import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.repositories.impls.sql.mappers.TrackedLinkMapper;
import backend.academy.scrapper.repositories.impls.sql.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
@ConditionalOnProperty(prefix = "app", name = "db-access-impl", havingValue = "sql")
public class SqlUserRepository implements UserRepository {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private UserMapper mapper;
    @Autowired
    private TrackedLinkMapper linkMapper;
    @Autowired
    private SqlLinkRepository linkRepository;

    @Override
    @Transactional
    public User save(User user) {
        jdbcTemplate.update("insert into users (id) values (:id)",
            new MapSqlParameterSource().addValue("id", user.id()));
        user.links().replaceAll(linkRepository::saveLinkOnly);
        return user;
    }

    @Override
    public boolean existsById(long id) {
        return jdbcTemplate.queryForObject(
            "select count(1) from users where id = :id",
            new MapSqlParameterSource().addValue("id", id), Integer.class) > 0;
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        var params = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update(
            "delete from tracked_link where id in " +
                "(select links_id from users_links where users_links.user_id = :id)",
            params);
        jdbcTemplate.update("delete from users_links where user_id = :id", params);
        jdbcTemplate.update("delete from users where id = :id", params);
    }

    @Override
    @Transactional
    public Optional<User> findById(long id) {
        var params = new MapSqlParameterSource().addValue("id", id);
        var res = jdbcTemplate.query("select * from users where id = :id",
            params, mapper);

        if (res.isEmpty())
            return Optional.empty();
        User user = res.getFirst();

        var links = jdbcTemplate.query(
            "select * from tracked_link where id in " +
                "(select links_id from users_links where users_links.user_id = :id)",
                params, linkMapper);

        user.links(links);

        return Optional.of(user);
    }
}

