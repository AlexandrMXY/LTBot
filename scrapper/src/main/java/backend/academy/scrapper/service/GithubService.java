package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.GithubUpdate;
import backend.academy.scrapper.model.github.Commit;
import backend.academy.scrapper.util.RequestErrorHandlers;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
@SuppressFBWarnings("REDOS")
public class GithubService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.from(ZoneOffset.UTC));

    private static final Pattern LINKS_HEADER_NEXT_PAGE_PATTERN =
            Pattern.compile("<((http(s)?://)?api.github.com/(?<url>[\\w/?=&]+))>;\\s*rel=\"next\"");

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

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
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
        return client.get()
                .uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, RequestErrorHandlers::logAndThrow)
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
