package backend.academy.scrapper.entities.filters;

import backend.academy.scrapper.dto.updates.Update;

public record NameFilter(String name) implements Filter {
    @Override
    public boolean validate(Update filters) {
        return !name.equals(filters.author());
    }
}
