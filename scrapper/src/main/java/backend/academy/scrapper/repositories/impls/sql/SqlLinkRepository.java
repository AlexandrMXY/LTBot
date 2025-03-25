package backend.academy.scrapper.repositories.impls.sql;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.impls.sql.mappers.TrackedLinkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


// TODO исправить delete'ы в обоих репозитория
@Repository
@ConditionalOnProperty(prefix = "app", name = "db-access-impl", havingValue = "sql")
public class SqlLinkRepository implements LinkRepository {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TrackedLinkMapper mapper;

    @Override
    public boolean existsByUserAndMonitoringServiceAndServiceId(User user, String monitoringService, String serviceId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("usedId", user.id())
            .addValue("monitoringService", monitoringService)
            .addValue("serviceId", serviceId);
        int res = jdbcTemplate.queryForObject(
            "select count(1) from tracked_link tl where " +
                "tl.user_id = :usedId and tl.monitoring_service = :monitoringService and tl.service_id = :serviceId",
            parameterSource, Integer.class);
        return res > 0;
    }

    TrackedLink saveLinkOnly(TrackedLink link) {
        long id = jdbcTemplate.queryForObject("select nextval('tracked_link_seq')",
            new MapSqlParameterSource(), Long.class);

        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("userId", link.user().id())
            .addValue("url", link.url())
            .addValue("monitoringService", link.monitoringService())
            .addValue("tags", mapper.converter().convertToDatabaseColumn(link.tags()))
            .addValue("filters", mapper.converter().convertToDatabaseColumn(link.filters()))
            .addValue("serviceId", link.serviceId())
            .addValue("lastUpdate", link.lastUpdate());


        jdbcTemplate.update(
            "insert into tracked_link (id, user_id, monitoring_service, service_id, tags, url, filters, last_update) " +
                "values (:id, :userId, :monitoringService, :serviceId, :tags, :url, :filters, :lastUpdate)", parameterSource);

        jdbcTemplate.update("insert into users_links (links_id, user_id) VALUES (:id, :userId)", parameterSource);

        link.id(id);
        return link;
    }

    @Override
    public TrackedLink save(TrackedLink link) {
        SqlParameterSource idSource = new MapSqlParameterSource().addValue("userId", link.user().id());

        if (jdbcTemplate.queryForObject("select count(1) from users where id = :userId;", idSource, Integer.class) == 0)
            jdbcTemplate.update("insert into users (id) values (:userId)", idSource);

        return saveLinkOnly(link);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteById(long id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("id", id);
//        jdbcTemplate.update("delete from users_links where links_id = :id", parameterSource);
        jdbcTemplate.update("delete from tracked_link where id = :id", parameterSource);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteByUserAndUrl(User u, String url) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("userId", u.id())
            .addValue("url", url);

        jdbcTemplate.update("delete from tracked_link where user_id = :userId and url = :url", parameterSource);
    }

    @Override
    @Transactional
    public Page<TrackedLink> findAllByMonitoringServiceAndLastUpdateLessThanOrderById(String monitoring, long lastUpdate, Pageable pageable) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("monitoringService", monitoring)
            .addValue("lastUpdate", lastUpdate)
            .addValue("offset", pageable.getOffset())
            .addValue("pageSize", pageable.getPageSize());

        var res = jdbcTemplate.queryForStream(
            "select * from tracked_link tl inner join public.users u on u.id = tl.user_id " +
                "where tl.monitoring_service = :monitoringService and tl.last_update < :lastUpdate " +
                "order by tl.id " +
                "offset :offset " +
                "fetch first :pageSize rows only",
            parameterSource, mapper);

        int totalCnt = jdbcTemplate.queryForObject(
            "select count(*) from tracked_link tl " +
                "where tl.monitoring_service = :monitoringService and tl.last_update < :lastUpdate",
            parameterSource, Integer.class);

        return new PageImpl<>(res.toList(), pageable, totalCnt);
    }

    @Override
    public void updateAllByMonitoringServiceAndServiceIdIsIn(Long newLastUpdate, String monitoringService, List<String> sIds) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("monitoringService", monitoringService)
            .addValue("newLastUpdate", newLastUpdate)
            .addValue("ids", sIds);

        jdbcTemplate.update(
            "update tracked_link tl set last_update = :newLastUpdate " +
                "where tl.monitoring_service = :monitoringService and tl.service_id in (:ids)",
            parameterSource);
    }
}
