package backend.academy.scrapper.model.github;

public record Commit(String url, CommitDetails commit) {

    public record CommitDetails(String url) {}
}
