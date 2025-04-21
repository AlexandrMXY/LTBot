package backend.academy.scrapper.entities.filters;

import backend.academy.scrapper.dto.updates.Update;

public sealed interface Filter permits NameFilter {
    boolean validate(Update filters);
}
