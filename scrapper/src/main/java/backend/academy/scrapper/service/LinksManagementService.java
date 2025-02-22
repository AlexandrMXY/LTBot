package backend.academy.scrapper.service;

import backend.academy.api.exceptions.InvalidRequestException;
import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.exceptions.UnsupportedLinkException;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.service.monitoring.LinkDistributionService;
import backend.academy.scrapper.service.monitoring.LinkMonitor;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LinksManagementService {
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ScrapperConfig config;
    @Autowired
    private LinkDistributionService linkDistributionService;

    private Pattern tagsPatter;

    @PostConstruct
    private void init() {
        tagsPatter = Pattern.compile(config.tagsFilterRegex());
    }

    @Transactional
    public LinkDto addLink(long userId, LinkDto link) {
        if(link.tags().stream().anyMatch((tag) -> !tagsPatter.matcher(tag).matches())) {
            throw new InvalidRequestException("Invalid tags");
        }

        String linkMonitor = linkDistributionService.findMonitor(link);
        if (linkMonitor == null) {
            throw new UnsupportedLinkException("Unsupported link: " + link.link());
        }

        LinkMonitor monitorInstance = linkDistributionService.getMonitor(linkMonitor);
        String serviceId = monitorInstance.getLinkId(link);

        User user = userRepository.findById(userId).orElse(new User(userId, new ArrayList<>()));

        if (linkRepository.existsByUserAndMonitoringServiceAndServiceId(user, linkMonitor, serviceId)) {
            throw new AlreadyExistsException("Link already exists");
        }

        TrackedLink trackedLink
            = new TrackedLink(0, user, link.link(), linkMonitor, link.tags(), serviceId);

        user.links().add(trackedLink);
        trackedLink = linkRepository.save(trackedLink);
        userRepository.save(user);
        return new LinkDto(trackedLink);
    }

    @Transactional
    public LinkDto deleteLink(long userId, String url) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        var linkOpt = user.links().stream().filter(link -> link.url().equals(url)).findAny();
        if (linkOpt.isEmpty()) {
            throw new NotFoundException("Link not found");
        }
        user.links().remove(linkOpt.orElseThrow());
        linkRepository.deleteById(linkOpt.orElseThrow().id());
        userRepository.save(user);
        return new LinkDto(linkOpt.orElseThrow());
    }

    @Transactional
    public List<LinkDto> getLinks(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        return user.links().stream().map((LinkDto::new)).collect(Collectors.toList());
    }


}
