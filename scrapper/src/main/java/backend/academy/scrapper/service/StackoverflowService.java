package backend.academy.scrapper.service;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.updates.StackoverflowUpdate;
import backend.academy.scrapper.model.stackoverflow.AnswerResponse;
import backend.academy.scrapper.model.stackoverflow.AnswersResponse;
import backend.academy.scrapper.util.MapBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class StackoverflowService {
    private static final int MAX_IDS_PER_QUERY = 100;

    @Autowired
    @Qualifier("stackoverflowRestClient")
    private RestClient client;


    public List<StackoverflowUpdate> getUpdates(List<Long> questionIds, long fromDate) {
        log.info("Getting updates for {}, since {}", questionIds, fromDate);
        if (questionIds.isEmpty()) {
            return List.of();
        }
        return getUpdatesForAllQuestions(questionIds, fromDate).stream()
            .map(response -> new StackoverflowUpdate(
                response.owner().displayName(), response.creationDate(), response.questionId()))
            .toList();
    }

    private List<AnswerResponse> getUpdatesForAllQuestions(List<Long> questionIds, long fromDate) {
        int requestsCnt = questionIds.size() / MAX_IDS_PER_QUERY
            + (questionIds.size() % MAX_IDS_PER_QUERY == 0 ? 0 : 1);
        List<AnswerResponse> result = new ArrayList<>();
        for (int i = 0; i < requestsCnt; i++) {
            result.addAll(getUpdates0(
                questionIds.subList(
                    i * MAX_IDS_PER_QUERY,
                    Math.clamp((long) (i + 1) * MAX_IDS_PER_QUERY, 0, questionIds.size())), fromDate));
        }

        return result;
    }

    private List<AnswerResponse> getUpdates0(List<Long> questionIds, long fromDate) {
        AnswersResponse response = null;
        List<AnswerResponse> result = new ArrayList<>();
        int page = 1;
        do {
            if (response != null) {
                result.addAll(response.items());
            }
            response = sendRequest(questionIds, fromDate, page);
            page++;
        } while (response.hasMore());
        log.info("Received {} responses: {}", result.size(), result);
        return result;
    }

    private AnswersResponse sendRequest(List<Long> questionIds, long fromDate, int page) {
        log.info("Sending request to: {}", buildUriString(questionIds, page, fromDate));
        return client.get()
            .uri(buildUriString(questionIds, page, fromDate))
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request0, response0) -> {
                log.error("Error request: {} -> {}",
                    request0.getURI().toString(),
                    new BufferedReader(
                        new InputStreamReader(response0.getBody())).lines().collect(Collectors.joining()));
                throw new ErrorResponseException(response0.getStatusCode());
            })
            .body(AnswersResponse.class);
    }

    private String buildUriString(List<Long> ids, int page, long fromdate) {
        return "/questions/"
            + ids.stream().map(String::valueOf).collect(Collectors.joining(";"))
            + "/answers?site={site}&key={key}&access_token={access_token}&page=" + page + "&fromdate=" + fromdate;
    }

}
