package backend.academy.scrapper.controllers;

import backend.academy.api.model.requests.TagsRequest;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.api.model.responses.TagsListResponse;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.service.TagsService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@TimeLimiter(name = TagsController.RESILIENCE4J_INSTANCE_NAME)
@RateLimiter(name = TagsController.RESILIENCE4J_INSTANCE_NAME)
public class TagsController {
    public static final String RESILIENCE4J_INSTANCE_NAME = "tags-controller";
    @Autowired
    private TagsService tagsService;

    @PostMapping("/tags/deactivate")
    public ResponseEntity<Void> deactivate(@RequestBody TagsRequest request) {
        tagsService.deactivateTag(request.userId(), request.tag());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/reactivate")
    public ResponseEntity<Void> reactivate(@RequestBody TagsRequest request) {
        tagsService.reactivateTag(request.userId(), request.tag());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tags/{id}")
    public TagsListResponse getDeactivatedTagsList(@PathVariable long id) {
        return new TagsListResponse(id, tagsService.getDeactivatedTagsList(id));
    }

    @GetMapping("/tags/linksWithTag")
    public ListLinksResponse getLinksWithTag(@RequestBody TagsRequest request) {
        return new ListLinksResponse(tagsService.getLinksWithTag(request.userId(), request.tag()).stream()
                .map(LinkDto::asResponse)
                .toList());
    }
}
