package backend.academy.scrapper.service.monitoring;

import backend.academy.scrapper.dto.LinkDto;
import org.springframework.stereotype.Component;

@Component
public class StackoverflowMonitor implements LinkMonitor {
    // TODO
    @Override
    public boolean isLinkValid(LinkDto link) {
        return link != null && link.link().contains("stack");
    }

    @Override
    public void checkForUpdates(LinkDto link) {

    }
}
