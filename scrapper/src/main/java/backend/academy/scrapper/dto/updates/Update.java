package backend.academy.scrapper.dto.updates;

import java.util.List;

public interface Update {
    List<Long> users();

    String url();

    String message();
}
