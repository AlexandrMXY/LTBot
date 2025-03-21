package backend.academy.scrapper.service.monitoring;

import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.dto.updates.Updates;
import java.util.function.Consumer;

public interface LinkMonitor {
    boolean isLinkValid(LinkDto link);

    String getLinkId(LinkDto link);

    void checkForUpdates(Consumer<Updates> updatesConsumer);
}
