package backend.academy.scrapper.repositories.impls.sql;

import backend.academy.scrapper.entities.MonitoringServiceData;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.util.StringListConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(prefix = "app", name = "db-access-impl", havingValue = "sql")
public abstract class SqlLinkRepository implements LinkRepository {

}
