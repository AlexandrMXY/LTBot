package backend.academy.scrapper.service.monitoring;

import backend.academy.scrapper.dto.GithubUpdate;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.entities.MonitoringServiceData;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.MonitoringServiceDataRepository;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.service.GithubService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@SuppressFBWarnings("REDOS")
public class GithubMonitor implements LinkMonitor {
    public static final String MONITOR_NAME = "githubMonitor";

    public static final Pattern GITHUB_LINK_PATTERN =
            Pattern.compile("^(http(s)?://)?github\\.com/(?<uid>[\\w\\-]+)/(?<rid>[\\w\\-]+)$");

    @Autowired
    private MonitoringServiceDataRepository monitoringServiceDataRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private GithubService githubService;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    private void init() {
        if (!monitoringServiceDataRepository.existsById(MONITOR_NAME)) {
            monitoringServiceDataRepository.save(new MonitoringServiceData(MONITOR_NAME, 0));
        }
    }

    @Override
    public boolean isLinkValid(LinkDto link) {
        return link != null && GITHUB_LINK_PATTERN.matcher(link.link()).matches();
    }

    @Override
    public String getLinkId(LinkDto link) {
        log.info(link.link());
        log.info(GITHUB_LINK_PATTERN.matcher(link.link()).namedGroups());
        Matcher matcher = GITHUB_LINK_PATTERN.matcher(link.link());
        if (!matcher.matches()) return null;
        return matcher.group("uid") + "/" + matcher.group("rid");
    }

    @Override
    @Transactional
    public Updates checkForUpdates() {
        var monitorData = monitoringServiceDataRepository.findById(MONITOR_NAME);
        Stream<TrackedLink> links = linkRepository.findAllByMonitoringService(MONITOR_NAME);
        long updateTime = System.currentTimeMillis() / 1000L;

        List<GithubUpdate> updates = githubService.getUpdates(
                links.map(TrackedLink::serviceId),
                monitorData
                        .orElseThrow(() -> new IllegalStateException("Github monitor data not found"))
                        .lastUpdate());

        Updates result = new Updates();

        for (GithubUpdate githubUpdate : updates) {
            result.addUpdate(new Updates.Update(
                    userRepository.findDistinctUserIdsWhereAnyLinkWithServiceId(githubUpdate.repo()),
                    getRepoLink(githubUpdate.repo()),
                    "Updated"));
        }

        monitoringServiceDataRepository.save(monitorData.orElseThrow().lastUpdate(updateTime));
        return result;
    }

    private String getRepoLink(String id) {
        return "https://github.com/" + id;
    }
}
