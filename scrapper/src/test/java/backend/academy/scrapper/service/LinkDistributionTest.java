package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.service.monitoring.GithubMonitor;
import backend.academy.scrapper.service.monitoring.LinkDistributionService;
import backend.academy.scrapper.service.monitoring.StackoverflowMonitor;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {LinkDistributionService.class})
public class LinkDistributionTest {
    @MockitoBean("githubMonitor")
    private GithubMonitor githubMonitor;

    @MockitoBean("stackoverflowMonitor")
    private StackoverflowMonitor stackoverflowMonitor;

    @Autowired
    private LinkDistributionService linkDistributionService;

    @BeforeEach
    public void init() {
        doCallRealMethod().when(githubMonitor).getLinkId(any());
        doCallRealMethod().when(githubMonitor).isLinkValid(any());
        doCallRealMethod().when(stackoverflowMonitor).getLinkId(any());
        doCallRealMethod().when(stackoverflowMonitor).isLinkValid(any());
    }

    @Test
    public void findMonitor_stackoverflowLink_returnStackoverflowMonitor() {
        assertEquals(
                "stackoverflowMonitor",
                linkDistributionService.findMonitor(new LinkDto(
                        "https://stackoverflow.com/questions/22349120/spring-profiles-on-method-level",
                        List.of(),
                        List.of(),
                        0)));
        assertEquals(
                "stackoverflowMonitor",
                linkDistributionService.findMonitor(new LinkDto(
                        "http://stackoverflow.com/questions/22349120/spring-profiles-on-method-level",
                        List.of(),
                        List.of(),
                        0)));
        assertEquals(
                "stackoverflowMonitor",
                linkDistributionService.findMonitor(new LinkDto(
                        "stackoverflow.com/questions/22349120/spring-profiles-on-method-level",
                        List.of(),
                        List.of(),
                        0)));
    }

    @Test
    public void findMonitor_githubLink_returnGithubMonitor() {
        assertEquals(
                "githubMonitor",
                linkDistributionService.findMonitor(
                        new LinkDto("https://github.com/pengrad/java-telegram-bot-api", List.of(), List.of(), 0)));
        assertEquals(
                "githubMonitor",
                linkDistributionService.findMonitor(
                        new LinkDto("http://github.com/pengrad/java-telegram-bot-api", List.of(), List.of(), 0)));
        assertEquals(
                "githubMonitor",
                linkDistributionService.findMonitor(
                        new LinkDto("github.com/pengrad/java-telegram-bot-api", List.of(), List.of(), 0)));
    }

    @Test
    public void findMonitor_unsupportedGithubLink_null() {
        assertNull(
                linkDistributionService.findMonitor(new LinkDto("http://github.com/pengrad", List.of(), List.of(), 0)));
    }

    @Test
    public void findMonitor_unsupportedStackoverflowLink_null() {
        assertNull(linkDistributionService.findMonitor(
                new LinkDto("https://stackoverflow.com/questions", List.of(), List.of(), 0)));
    }

    @Test
    public void findMonitor_unsupportedLink_null() {
        assertNull(
                linkDistributionService.findMonitor(new LinkDto("https://www.youtube.com", List.of(), List.of(), 0)));
    }

    @Test
    public void getServiceId_githubLink_returnValidResult() {
        assertEquals(
                "pengrad/java-telegram-bot-api",
                linkDistributionService.getServiceId(
                        new LinkDto("https://github.com/pengrad/java-telegram-bot-api", List.of(), List.of(), 0)));
    }

    @Test
    public void getServiceId_stackoverflowLink_returnValidResult() {
        assertEquals(
                "22349120",
                linkDistributionService.getServiceId(new LinkDto(
                        "https://stackoverflow.com/questions/22349120/spring-profiles-on-method-level",
                        List.of(),
                        List.of(),
                        0)));
    }

    @Test
    public void getServiceId_invalidLink_returnNull() {
        assertNull(
                linkDistributionService.getServiceId(new LinkDto("https://www.youtube.com", List.of(), List.of(), 0)));
    }
}
