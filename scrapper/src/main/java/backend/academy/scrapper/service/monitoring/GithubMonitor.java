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
public class GithubMonitor extends AbstractMonitor {
    public static final String MONITOR_NAME = "githubMonitor";

    public static final Pattern GITHUB_LINK_PATTERN =
            Pattern.compile("^(http(s)?://)?github\\.com/(?<uid>[\\w\\-]+)/(?<rid>[\\w\\-]+)$");

    @Autowired
    public GithubMonitor(
            @Qualifier("githubUpdatesCollector") LinkUpdatesCollector linkUpdatesCollector,
            LinkRepository linkRepository,
            ScrapperConfig config) {
        super(MONITOR_NAME, linkUpdatesCollector, linkRepository, config);
    }

    @Override
    public boolean isLinkValid(LinkDto link) {
        return link != null && GITHUB_LINK_PATTERN.matcher(link.link()).matches();
    }

    @Override
    public String getLinkId(LinkDto link) {
        Matcher matcher = GITHUB_LINK_PATTERN.matcher(link.link());
        if (!matcher.matches()) return null;
        return matcher.group("uid") + "/" + matcher.group("rid");
    }
}
