package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.updates.StackoverflowUpdate;
import backend.academy.scrapper.model.stackoverflow.AnswerResponse;
import backend.academy.scrapper.model.stackoverflow.AnswersResponse;
import backend.academy.scrapper.util.RequestErrorHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class StackoverflowService {
    private static final int MAX_IDS_PER_QUERY = 100;

    @Autowired
    @Qualifier("stackoverflowRestClient")
    private RestClient client;

    public List<StackoverflowUpdate> getUpdates(List<Long> questionIds, long fromDate) {
        if (questionIds.isEmpty()) {
            return List.of();
        }
        return getUpdatesForAllQuestions(questionIds, fromDate).stream()
                .map(response -> new StackoverflowUpdate(
                        response.owner().displayName(), response.creationDate(), response.questionId()))
                .toList();
    }

    private List<AnswerResponse> getUpdatesForAllQuestions(List<Long> questionIds, long fromDate) {
        int requestsCnt =
                questionIds.size() / MAX_IDS_PER_QUERY + (questionIds.size() % MAX_IDS_PER_QUERY == 0 ? 0 : 1);
        List<AnswerResponse> result = new ArrayList<>();
        for (int i = 0; i < requestsCnt; i++) {
            result.addAll(getUpdates0(
                    questionIds.subList(
                            i * MAX_IDS_PER_QUERY,
                            Math.clamp((long) (i + 1) * MAX_IDS_PER_QUERY, 0, questionIds.size())),
                    fromDate));
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
        return result;
    }

    private AnswersResponse sendRequest(List<Long> questionIds, long fromDate, int page) {
        log.info("Sending request to: {}", buildUriString(questionIds, page, fromDate));
        return client.get()
                .uri(buildUriString(questionIds, page, fromDate))
                .retrieve()
                .onStatus(HttpStatusCode::isError, RequestErrorHandlers::logAndThrow)
                .body(AnswersResponse.class);
    }

    private String buildUriString(List<Long> ids, int page, long fromdate) {
        return "/questions/"
                + ids.stream().map(String::valueOf).collect(Collectors.joining(";"))
                + "/answers?site={site}&key={key}&access_token={access_token}&page=" + page + "&fromdate=" + fromdate;
    }
}
