package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.GithubUpdate;
import backend.academy.scrapper.model.github.Commit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.RestClient;

@Service
@Log4j2
public class GithubService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss'Z'").withZone(ZoneId.from(ZoneOffset.UTC));

    private static final Pattern LINKS_HEADER_NEXT_PAGE_PATTERN =
            Pattern.compile("\\<((http(s)?://)?api.github.com/(?<url>[\\w/?=&]+))\\>;\\s*rel=\\\"next\\\"");

    @Autowired
    @Qualifier("githubRestClient")
    private RestClient client;

    public List<GithubUpdate> getUpdates(Stream<String> reposIds, long fromDate) {
        List<GithubUpdate> updates = new ArrayList<>();

        reposIds.forEach((repo) -> {
            List<Commit> newCommits = getAllCommits(repo, fromDate);
            newCommits.stream().map(commit -> new GithubUpdate(repo)).forEach(updates::add);
        });

        return updates;
    }

    private List<Commit> getAllCommits(String repo, long fromDate) {
        String nextURL = "/repos/" + repo + "/commits?since=" + getDateString(fromDate);
        List<Commit> result = new ArrayList<>();
        do {
            ResponseEntity<Commit[]> response = sendRequest(nextURL);
            nextURL = getNextRequestUrl(response.getHeaders().getFirst("Link"));
            Collections.addAll(result, response.getBody());
        } while (nextURL != null);
        return result;
    }

    private ResponseEntity<Commit[]> sendRequest(String uri) {
        log.info("Sending request to {}", uri);
        return client.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request0, response0) -> {
                    log.error(
                            "Error request: {} -> {}",
                            request0.getURI().toString(),
                            new BufferedReader(new InputStreamReader(response0.getBody()))
                                    .lines()
                                    .collect(Collectors.joining()));
                    throw new ErrorResponseException(response0.getStatusCode());
                })
                .toEntity(Commit[].class);
    }

    private String getDateString(long fromDate) {
        return DATE_TIME_FORMATTER.format(Instant.ofEpochSecond(fromDate));
    }

    private String getNextRequestUrl(String responseLinksHeader) {
        if (responseLinksHeader == null) {
            return null;
        }

        Matcher matcher = LINKS_HEADER_NEXT_PAGE_PATTERN.matcher(responseLinksHeader);

        if (!matcher.find()) {
            return null;
        }

        return matcher.group("url");
    }
}
