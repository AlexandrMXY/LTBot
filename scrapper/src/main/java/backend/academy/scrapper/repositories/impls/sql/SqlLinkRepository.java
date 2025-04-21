package backend.academy.scrapper.repositories.impls.sql;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.impls.sql.mappers.TrackedLinkMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "sql")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SqlLinkRepository implements LinkRepository {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private TrackedLinkMapper mapper;

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean existsByUserAndMonitoringServiceAndServiceId(User user, String monitoringService, String serviceId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("usedId", user.id())
                .addValue("monitoringService", monitoringService)
                .addValue("serviceId", serviceId);
        Integer res = jdbcTemplate.queryForObject(
                "select count(1) from tracked_link tl where "
                        + "tl.user_id = :usedId and tl.monitoring_service = :monitoringService and tl.service_id = :serviceId",
                parameterSource,
                Integer.class);
        return res != null && res > 0;
    }

    @SuppressWarnings("ConstantConditions")
    TrackedLink saveLinkOnly(TrackedLink link) {
        Long id = jdbcTemplate.queryForObject(
                "select nextval('tracked_link_seq')", new MapSqlParameterSource(), Long.class);
        if (id == null) return null;

        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("userId", link.user().id())
                .addValue("url", link.url())
                .addValue("monitoringService", link.monitoringService())
                .addValue("tags", mapper.converter().convertToDatabaseColumn(link.tags()))
                .addValue("filters", mapper.filtersConverter().convertToDatabaseColumn(link.filters()))
                .addValue("serviceId", link.serviceId())
                .addValue("lastUpdate", link.lastUpdate());

        jdbcTemplate.update(
                "insert into tracked_link (id, user_id, monitoring_service, service_id, tags, url, filters, last_update) "
                        + "values (:id, :userId, :monitoringService, :serviceId, :tags, :url, :filters, :lastUpdate)",
                parameterSource);

        link.id(id);
        return link;
    }

    @Transactional
    public void update(TrackedLink link) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("filters", mapper.filtersConverter().convertToDatabaseColumn(link.filters()))
                .addValue("tags", mapper.converter().convertToDatabaseColumn(link.tags()))
                .addValue("lastUpdate", link.lastUpdate())
                .addValue("monitoringService", link.monitoringService())
                .addValue("url", link.url())
                .addValue("serviceId", link.serviceId())
                .addValue("id", link.id())
                .addValue("userId", link.user().id());
        jdbcTemplate.update(
                """
                update tracked_link set filters = :filters,
                    tags = :tags,
                    last_update = :lastUpdate,
                    monitoring_service = :monitoringService,
                    url = :url,
                    user_id = :userId,
                    service_id = :serviceId
                where id = :id""",
                parameterSource);
    }

    @Override
    @Transactional
    @SuppressWarnings("ConstantConditions")
    public TrackedLink save(TrackedLink link) {
        SqlParameterSource parameterSource =
                new MapSqlParameterSource().addValue("userId", link.user().id()).addValue("linkId", link.id());

        Integer userExistsQueryResult = jdbcTemplate.queryForObject(
                "select count(1) from users where id = :userId;", parameterSource, Integer.class);
        if (userExistsQueryResult == null || userExistsQueryResult == 0)
            jdbcTemplate.update("insert into users (id) values (:userId)", parameterSource);

        Integer linkExistsQueryResult = jdbcTemplate.queryForObject(
                "select count(1) from tracked_link where id = :linkId", parameterSource, Integer.class);
        if (linkExistsQueryResult == null || linkExistsQueryResult == 0) return saveLinkOnly(link);

        update(link);
        return link;
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("id", id);
        jdbcTemplate.update("delete from tracked_link where id = :id", parameterSource);
    }

    @Override
    @Transactional
    @SuppressWarnings("ConstantConditions")
    public Page<TrackedLink> findAllByMonitoringServiceAndLastUpdateLessThanOrderById(
            String monitoring, long lastUpdate, Pageable pageable) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("monitoringService", monitoring)
                .addValue("lastUpdate", lastUpdate)
                .addValue("offset", pageable.getOffset())
                .addValue("pageSize", pageable.getPageSize());

        var res = jdbcTemplate.queryForStream(
                """
                select * from tracked_link tl inner join public.users u on u.id = tl.user_id \
                where tl.monitoring_service = :monitoringService and tl.last_update < :lastUpdate \
                order by tl.id \
                offset :offset \
                fetch first :pageSize rows only""",
                parameterSource,
                mapper);

        Integer totalCnt = jdbcTemplate.queryForObject(
                "select count(*) from tracked_link tl "
                        + "where tl.monitoring_service = :monitoringService and tl.last_update < :lastUpdate",
                parameterSource,
                Integer.class);
        if (totalCnt == null) return new PageImpl<>(new ArrayList<>(), pageable, 0);

        return new PageImpl<>(res.toList(), pageable, totalCnt);
    }

    @Override
    @Transactional
    public void updateAllByMonitoringServiceAndServiceIdIsIn(
            Long newLastUpdate, String monitoringService, List<String> sIds) {
        if (sIds == null || sIds.isEmpty()) return;

        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("monitoringService", monitoringService)
                .addValue("newLastUpdate", newLastUpdate)
                .addValue("ids", sIds);

        jdbcTemplate.update(
                "update tracked_link tl set last_update = :newLastUpdate "
                        + "where tl.monitoring_service = :monitoringService and tl.service_id in (:ids)",
                parameterSource);
    }

    @Override
    public List<TrackedLink> findAllByUserId(long userId) {
        SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("userId", userId);

        var res = jdbcTemplate.queryForStream(
                "select * from tracked_link tl where tl.user_id = :userId", parameterSource, mapper);

        return res.toList();
    }

    @Override
    public Optional<TrackedLink> findByUserIdAndUrl(long userId, String url) {
        SqlParameterSource parameterSource =
                new MapSqlParameterSource().addValue("userId", userId).addValue("url", url);

        Stream<TrackedLink> res = jdbcTemplate.queryForStream(
                "select * from tracked_link tl where tl.user_id = :userId and url like :url limit 1",
                parameterSource,
                mapper);
        List<TrackedLink> resList = res.toList();
        return resList.isEmpty() ? Optional.empty() : Optional.of(resList.getFirst());
    }
}
