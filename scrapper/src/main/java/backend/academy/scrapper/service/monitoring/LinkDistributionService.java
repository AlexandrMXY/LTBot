package backend.academy.scrapper.service.monitoring;

import backend.academy.scrapper.dto.LinkDto;
import jakarta.persistence.ManyToOne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Service
public class LinkDistributionService {
    @Autowired
    private Map<String, LinkMonitor> monitors;

    public String findMonitor(LinkDto linkDto) {
        for (var entry : monitors.entrySet()) {
            if (entry.getValue().isLinkValid(linkDto)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public LinkMonitor getMonitor(String monitorName) {
        return monitors.get(monitorName);
    }


    public Collection<LinkMonitor> getMonitors() {
        return monitors.values();
    }
}
