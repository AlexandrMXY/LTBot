package backend.academy.scrapper.repositories.impls.sql.mappers;

import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.util.FiltersConverter;
import backend.academy.scrapper.util.StringListConverter;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
@Getter
public class TrackedLinkMapper implements RowMapper<TrackedLink> {
    private final StringListConverter converter = new StringListConverter();
    private final FiltersConverter filtersConverter = new FiltersConverter();

    @Override
    public @NotNull TrackedLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TrackedLink(
                rs.getLong("id"),
                new User(rs.getLong("user_id"), null),
                rs.getString("url"),
                rs.getString("monitoring_service"),
                converter.convertToEntityAttribute(rs.getString("tags")),
                filtersConverter.convertToEntityAttribute(rs.getString("filters")),
                rs.getString("service_id"),
                rs.getLong("last_update"));
    }
}
