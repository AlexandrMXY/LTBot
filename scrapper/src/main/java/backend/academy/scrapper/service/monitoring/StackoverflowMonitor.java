package backend.academy.scrapper.service.monitoring;

import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.dto.updates.StackoverflowUpdate;
import backend.academy.scrapper.entities.MonitoringServiceData;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.MonitoringServiceDataRepository;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.service.StackoverflowService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@SuppressFBWarnings("REDOS")
public class StackoverflowMonitor implements LinkMonitor {
    public static final String MONITOR_NAME = "stackoverflowMonitor";

    public static final Pattern STACKOVERFLOW_LINK_PATTERN =
            Pattern.compile("^(http(s)?://)?stackoverflow\\.com/questions/(?<id>\\d{1,})/[\\w\\-]*$");

    @Autowired
    private StackoverflowService stackoverflowService;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private MonitoringServiceDataRepository monitoringServiceDataRepository;

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
        return link != null && STACKOVERFLOW_LINK_PATTERN.matcher(link.link()).matches();
    }

    @Override
    public String getLinkId(LinkDto link) {
        Matcher matcher = STACKOVERFLOW_LINK_PATTERN.matcher(link.link());
        if (!matcher.matches()) return null;
        return matcher.group("id");
    }

    @Override
    public Updates checkForUpdates() {
        var monitorData = monitoringServiceDataRepository.findById(MONITOR_NAME);
        Stream<TrackedLink> links = linkRepository.findAllByMonitoringService(MONITOR_NAME);
        long updateTime = System.currentTimeMillis() / 1000L;

        log.atInfo()
                .setMessage("Checking links for updates")
                .addKeyValue("monitor", MONITOR_NAME)
                .log();

        var updates = stackoverflowService.getUpdates(
                links.map(link -> Long.parseLong(link.serviceId())).toList(),
                monitorData
                        .orElseThrow(() -> new IllegalStateException("Stackoverflow monitor data not found"))
                        .lastUpdate());

        Updates result = new Updates();

        for (StackoverflowUpdate stackoverflowUpdate : updates) {
            result.addUpdate(new Updates.Update(
                    userRepository.findDistinctUserIdsWhereAnyLinkWithServiceId(
                            String.valueOf(stackoverflowUpdate.questionId())),
                    linkToQuestionWithId(String.valueOf(stackoverflowUpdate.questionId())),
                    "Updated"));
        }

        monitoringServiceDataRepository.save(monitorData.orElseThrow().lastUpdate(updateTime));
        return result;
    }

    private String linkToQuestionWithId(String id) {
        return "https://stackoverflow.com/questions/" + id;
    }
}
