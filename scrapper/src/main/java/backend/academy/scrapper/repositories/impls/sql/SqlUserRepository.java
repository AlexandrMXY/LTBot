package backend.academy.scrapper.repositories.impls.sql;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.repositories.impls.sql.mappers.TrackedLinkMapper;
import backend.academy.scrapper.repositories.impls.sql.mappers.UserMapper;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "sql")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
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
        if (existsById(user.id())) {
            update(user);
            return user;
        }

        jdbcTemplate.update( """
                insert into users (id, inactive_tags, notification_policy, notification_time) \
                values (:id, :inactiveTags, :notificationPolicy, :notificationTime)""",
                new MapSqlParameterSource()
                        .addValue("id", user.id())
                        .addValue("inactiveTags", mapper.converter().convertToDatabaseColumn(user.inactiveTags()))
                        .addValue("notificationPolicy", user.notificationStrategy().toString())
                        .addValue("notificationTime", user.notificationTime()));
        user.links(user.links().stream()
                .map(linkRepository::saveLinkOnly)
                .collect(Collectors.toCollection(ArrayList::new)));
        return user;
    }

    private void update(User user) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("userId", user.id())
                .addValue("inactiveTags", mapper.converter().convertToDatabaseColumn(user.inactiveTags()))
                .addValue("linksIds", user.links().stream().map(TrackedLink::id).toList())
                .addValue("notificationPolicy", user.notificationStrategy().toString())
                .addValue("notificationTime", user.notificationTime());

        jdbcTemplate.update("""
            update users set
                inactive_tags = :inactiveTags,
                notification_policy = :notificationPolicy,
                notification_time = :notificationTime
            where id = :userId
            """, parameterSource);
        if (!user.links().isEmpty())
            jdbcTemplate.update(
                    "delete from tracked_link where user_id = :userId and id not in (:linksIds)", parameterSource);
        for (TrackedLink tl : user.links()) linkRepository.save(tl);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean existsById(long id) {
        var res = jdbcTemplate.queryForObject(
                "select count(1) from users where id = :id",
                new MapSqlParameterSource().addValue("id", id),
                Integer.class);
        return res != null && res > 0;
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        var params = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update("delete from tracked_link where tracked_link.user_id = :id", params);
        jdbcTemplate.update("delete from users where id = :id", params);
    }

    @Override
    @Transactional
    public Optional<User> findById(long id) {
        var params = new MapSqlParameterSource().addValue("id", id);
        var res = jdbcTemplate.query("select * from users where id = :id", params, mapper);

        if (res.isEmpty()) return Optional.empty();
        User user = res.getFirst();

        var links =
                jdbcTemplate.query("select * from tracked_link where tracked_link.user_id = :id", params, linkMapper);

        user.links(links);

        return Optional.of(user);
    }
}
