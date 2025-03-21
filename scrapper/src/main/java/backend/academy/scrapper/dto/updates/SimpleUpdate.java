package backend.academy.scrapper.dto.updates;

import java.util.List;

public record SimpleUpdate(List<Long> users, String url, String message) implements Update {
}
