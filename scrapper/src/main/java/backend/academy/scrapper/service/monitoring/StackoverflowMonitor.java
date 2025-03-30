package backend.academy.scrapper.service.monitoring;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.service.monitoring.collectors.LinkUpdatesCollector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@SuppressFBWarnings("REDOS")
public class StackoverflowMonitor extends AbstractMonitor {
    public static final String MONITOR_NAME = "stackoverflowMonitor";

    public static final Pattern STACKOVERFLOW_LINK_PATTERN =
            Pattern.compile("^(http(s)?://)?stackoverflow\\.com/questions/(?<id>\\d{1,})/[\\w\\-]*$");

    @Autowired
    public StackoverflowMonitor(
            @Qualifier("stackoverflowUpdatesCollector") LinkUpdatesCollector linkUpdatesCollector,
            LinkRepository linkRepository,
            ScrapperConfig config) {
        super(MONITOR_NAME, linkUpdatesCollector, linkRepository, config);
    }

    @Override
    public boolean isLinkValid(LinkDto link) {
        return link != null && STACKOVERFLOW_LINK_PATTERN.matcher(link.link()).matches();
    }

    @Override
    public String getLinkId(LinkDto link) {
        Matcher matcher = STACKOVERFLOW_LINK_PATTERN.matcher(link.link());
        if (!matcher.matches()) return null;
        return matcher.group("id");
    }
}
