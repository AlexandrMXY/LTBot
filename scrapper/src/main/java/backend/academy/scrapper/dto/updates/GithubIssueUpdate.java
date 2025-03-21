package backend.academy.scrapper.dto.updates;

import java.util.List;

public record GithubIssueUpdate(
    long user,
    String htmlUrl,
    String preview,
    String author
) implements Update {
    @Override
    public List<Long> users() {
        return List.of(user);
    }

    @Override
    public String url() {
        return htmlUrl;
    }

    @Override
    public String message() {
        // TODO
        return "TODO";
    }
}
