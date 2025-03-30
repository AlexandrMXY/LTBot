package backend.academy.scrapper.service.monitoring.collectors;

import backend.academy.scrapper.dto.updates.UpdateImpl;
import backend.academy.scrapper.dto.updates.Update;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.model.github.Issue;
import backend.academy.scrapper.util.RequestErrorHandlers;
import backend.academy.scrapper.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class GithubUpdatesCollector implements LinkUpdatesCollector {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.from(ZoneOffset.UTC));

    private static final Pattern LINKS_HEADER_NEXT_PAGE_PATTERN =
            Pattern.compile("<(?<url>[^>]*)>;\\s*rel=\"next\"");

    private static final String ISSUES_ENDPOINT = "issues";


    @Autowired
    @Qualifier("githubRestClient")
    private RestClient client;

    @Override
    public Updates getUpdates(Stream<TrackedLink> links) {
        return links.map(this::getUpdates).reduce(Updates::mergeResult).orElseGet(Updates::new);
    }

    private Updates getUpdates(TrackedLink tl) {
        var issues = receiveAllPages(tl.serviceId(), ISSUES_ENDPOINT, tl.lastUpdate(), Issue[].class);

        return new Updates().addUpdates(
            issues.stream()
                .map(i -> (Update) new UpdateImpl(
                    tl.user().id(),
                    convertDate(i.createdAt()),
                    i.htmlUrl(),
                    StringUtils.clamp(i.body(), MAX_PREVIEW_LENGTH),
                    i.user().login()))
                .toList());
    }

    private <T> List<T> receiveAllPages(String serviceId, String endpoint, long since, Class<T[]> responseType) {
        String url = repoUrl(serviceId) + "/" + endpoint + "?since=" + getDateString(since);
        return receiveAllPages(url, responseType);
    }
    private <T> List<T> receiveAllPages(String url, Class<T[]> responseType) {
        String nextURL = url;

        List<T> result = new ArrayList<>();
        do {
            ResponseEntity<T[]> response = sendRequest(nextURL, responseType);
            nextURL = getNextRequestUrl(response.getHeaders().getFirst("Link"));
            Collections.addAll(result, response.getBody());
        } while (nextURL != null);

        return result;
    }

    private <T> ResponseEntity<T[]> sendRequest(String url, Class<T[]> responseType) {
        return client.get()
            .uri(url)
            .retrieve()
            .onStatus(HttpStatusCode::isError, RequestErrorHandlers::logAndThrow)
            .toEntity(responseType);
    }

    private static String repoUrl(String serviceId) {
        return "repos/" + serviceId;
    }

    private String getDateString(long fromDate) {
        return DATE_TIME_FORMATTER.format(Instant.ofEpochSecond(fromDate));
    }

    private String getNextRequestUrl(String responseLinksHeader) {
        if (responseLinksHeader == null)
            return null;

        Matcher matcher = LINKS_HEADER_NEXT_PAGE_PATTERN.matcher(responseLinksHeader);

        if (!matcher.find())
            return null;

        return matcher.group("url");
    }

    private long convertDate(String dateStr) {
        TemporalAccessor accessor =  DATE_TIME_FORMATTER.parse(dateStr);
        Instant instant = Instant.from(accessor);
        return Instant.EPOCH.until(instant, ChronoUnit.SECONDS);
    }
}
