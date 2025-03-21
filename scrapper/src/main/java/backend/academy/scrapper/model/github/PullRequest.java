package backend.academy.scrapper.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PullRequest(
    String url,
    @JsonProperty("html_url") String htmlUrl
) {

}
