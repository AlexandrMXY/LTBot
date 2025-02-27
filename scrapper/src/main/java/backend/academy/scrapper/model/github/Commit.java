package backend.academy.scrapper.model.github;

public record Commit(String url, CommitDetails commit) {

    public static record CommitDetails(String url) {}
}
