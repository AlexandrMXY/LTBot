package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Issue(
        String url,
        @JsonProperty("html_url") String htmlUrl,
        String title,
        @JsonProperty("comments_url") String commentsUrl,
        GHUser user,
        String body,
        @JsonProperty("pull_request") PullRequest pullRequest,
        @JsonProperty("created_at") String createdAt) {}
