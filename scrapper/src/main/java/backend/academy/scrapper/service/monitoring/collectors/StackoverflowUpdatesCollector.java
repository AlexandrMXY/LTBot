package backend.academy.scrapper.service.monitoring.collectors;

import backend.academy.scrapper.dto.updates.UpdateImpl;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.model.stackoverflow.AnswerResponse;
import backend.academy.scrapper.model.stackoverflow.AnswersResponse;
import backend.academy.scrapper.model.stackoverflow.CommentResponse;
import backend.academy.scrapper.model.stackoverflow.CommentsResponse;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.util.RequestErrorHandlers;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Flux;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class StackoverflowUpdatesCollector implements LinkUpdatesCollector {
    private static final int MAX_IDS_PER_REQUEST = 100;

    @Autowired
    @Qualifier("stackoverflowRestClient")
    private RestClient client;


    @Override
    public Updates getUpdates(Stream<TrackedLink> links) {
        return links
            .reduce(new Updates(), (updates, link) ->
                updates.mergeResult(getLinkUpdates(link)), Updates::mergeResult);
    }

    private Updates getLinkUpdates(TrackedLink link) {
        List<AnswerResponse> answers = getAllAnswers(link.serviceId());

        ArrayList<CommentResponse> comments = new ArrayList<>();
        StringBuilder idsBuilder = new StringBuilder();
        for (int i = 0; i < answers.size(); i++) {
            if (!idsBuilder.isEmpty())
                idsBuilder.append(";");
            idsBuilder.append(answers.get(i).answerId());

            if (i % MAX_IDS_PER_REQUEST == MAX_IDS_PER_REQUEST - 1 || i == answers.size() - 1) {
                comments.addAll(getAllComments(idsBuilder.toString(), link.lastUpdate()));
                idsBuilder.setLength(0);
            }
        }

        Updates updates = new Updates();

        answers.stream()
            .filter((ans) -> ans.creationDate() >= link.lastUpdate())
            .forEach((ans) -> {
                updates.addUpdate(new UpdateImpl(
                    link.user().id(),
                    ans.creationDate(),
                    link.url(),
                    ans.body(),
                    ans.owner().displayName()
                ));
            });

        comments.forEach((comment) -> {
            updates.addUpdate(new UpdateImpl(
                link.user().id(),
                comment.creationDate(),
                link.url(),
                comment.body(),
                comment.owner().displayName()
            ));
        });

        return updates;
    }

    private List<AnswerResponse> getAllAnswers(String id) {
        String baseRequestStr = String.format("/questions/%s/answers?site=stackoverflow&page={page}", id);

        List<AnswerResponse> result = new ArrayList<>();
        AnswersResponse response;
        int pageId = 1;

        do {
            response = client.get()
                .uri(baseRequestStr, pageId++)
                .retrieve()
                .onStatus(HttpStatusCode::isError, RequestErrorHandlers::logAndThrow)
                .toEntity(AnswersResponse.class)
                .getBody();
            result.addAll(response.items());
        } while (response.hasMore());

        return result;
    }

    private List<CommentResponse> getAllComments(String idsStr, long since) {
        String baseRequestStr =
            String.format("/answers/%s/comments?site=stackoverflow&since=%d&page={page}", idsStr, since);

        List<CommentResponse> result = new ArrayList<>();
        CommentsResponse response;
        int pageId = 1;

        do {
            response = client.get()
                .uri(baseRequestStr, pageId++)
                .retrieve()
                .onStatus(HttpStatusCode::isError, RequestErrorHandlers::logAndThrow)
                .toEntity(CommentsResponse.class)
                .getBody();
            result.addAll(response.items());
        } while (response.hasMore());

        return result;
    }

}

