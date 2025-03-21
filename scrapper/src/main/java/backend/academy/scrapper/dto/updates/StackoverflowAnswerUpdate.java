package backend.academy.scrapper.dto.updates;

import java.util.List;

public record StackoverflowAnswerUpdate(
    long user,
    long date,
    long questionId,
    String creator,
    String content
) implements Update {
    @Override
    public List<Long> users() {
        return List.of(user);
    }

    @Override
    public String url() {
        return "https://stackoverflow.com/questions/" + questionId;
    }

    @Override
    public String message() {
        // TODO
        return "TODO";
    }
}
