package backend.academy.scrapper.entities.filters;

import backend.academy.scrapper.dto.updates.Update;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class Filters {
    private final List<Filter> filters = new ArrayList<>();

    public void addFilter(Filter filter) {
        if (filter != null) {
            filters.add(filter);
        }
    }

    public boolean isEmpty() {
        return filters.isEmpty();
    }

    public boolean validate(Update update) {
        for (var filters : filters) {
            if (!filters.validate(update)) {
                return false;
            }
        }
        return true;
    }
}
