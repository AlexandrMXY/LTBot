package backend.academy.scrapper.repositories.impls.sql.mappers;

import backend.academy.scrapper.entities.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
public class UserMapper implements RowMapper<User> {
    @Override
    public @NotNull User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(rs.getLong("id"), new ArrayList<>());
    }
}
