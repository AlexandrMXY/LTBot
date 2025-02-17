package backend.academy.scrapper.service.monitoring;

import backend.academy.scrapper.dto.LinkDto;

public interface LinkMonitor {
    boolean isLinkValid(LinkDto link);

    String getLinkId(LinkDto link);

    UpdateResult checkForUpdates();
}
