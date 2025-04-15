package backend.academy.scrapper.service.monitoring.collectors;

import backend.academy.scrapper.dto.updates.UpdateImpl;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.model.stackoverflow.AnswerResponse;
import backend.academy.scrapper.model.stackoverflow.AnswersResponse;
import backend.academy.scrapper.model.stackoverflow.CommentResponse;
import backend.academy.scrapper.model.stackoverflow.CommentsResponse;
import backend.academy.scrapper.util.StringUtils;
import backend.academy.scrapper.web.clients.StackoverflowRestClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StackoverflowUpdatesCollector implements LinkUpdatesCollector {
    private static final int MAX_IDS_PER_REQUEST = 100;

    @Autowired
    private StackoverflowRestClient client;

    @Override
    public Updates getUpdates(Stream<TrackedLink> links) {
        return links.reduce(
                new Updates(), (updates, link) -> updates.mergeResult(getLinkUpdates(link)), Updates::mergeResult);
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
                .forEach((ans) -> updates.addUpdate(new UpdateImpl(
                        link.user().id(),
                        ans.creationDate(),
                        link.url(),
                        StringUtils.clamp(ans.body(), MAX_PREVIEW_LENGTH),
                        ans.owner().displayName())));

        comments.stream()
                .filter((comment) -> comment.creationDate() >= link.lastUpdate())
                .forEach((comment) -> {
                    updates.addUpdate(new UpdateImpl(
                            link.user().id(),
                            comment.creationDate(),
                            link.url(),
                            StringUtils.clamp(comment.body(), MAX_PREVIEW_LENGTH),
                            comment.owner().displayName()));
                });

        return updates;
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    private List<AnswerResponse> getAllAnswers(String id) {
        List<AnswerResponse> result = new ArrayList<>();
        AnswersResponse response;

        int pageId = 1;

        do {
            String baseRequestStr = String.format("/questions/%s/answers?site=stackoverflow&page=%d", id, pageId);
            response = client.getRequest(baseRequestStr, AnswersResponse.class);
            if (response != null && response.items() != null) result.addAll(response.items());
        } while (response.hasMore());

        return result;
    }

    @SuppressWarnings("ConstantConditions")
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    private List<CommentResponse> getAllComments(String idsStr, long since) {
        List<CommentResponse> result = new ArrayList<>();
        CommentsResponse response;
        int pageId = 1;

        do {
            String requestUri =
                String.format("/answers/%s/comments?site=stackoverflow&since=%d&page=%d", idsStr, since, pageId);
            response = client.getRequest(requestUri, CommentsResponse.class);
            if (response != null && response.items() != null) result.addAll(response.items());
        } while (response.hasMore());

        return result;
    }
}
