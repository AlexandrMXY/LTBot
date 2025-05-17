package backend.academy.scrapper.controllers;

import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.requests.LinkTagRequest;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.service.LinksService;
import backend.academy.scrapper.service.TagsService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
@RateLimiter(name = LinksController.RESILIENCE4J_INSTANCE_NAME)
public class LinksController {
    public static final String RESILIENCE4J_INSTANCE_NAME = "links-controller";

    @Autowired
    private LinksService linksService;

    @Autowired
    private TagsService tagsService;

    @GetMapping
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        return new ListLinksResponse(
                linksService.getLinks(chatId).stream().map(LinkDto::asResponse).toList());
    }

    @PostMapping
    public LinkResponse addLinks(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLinkRequest request) {
        return linksService.addLink(chatId, new LinkDto(request)).asResponse();
    }

    @DeleteMapping
    public LinkResponse deleteLinks(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLinkRequest request) {
        return linksService.deleteLink(chatId, request.link()).asResponse();
    }

    @PostMapping("/tags")
    public void addTag(@RequestBody LinkTagRequest request) {
        tagsService.addTagToLink(request.userId(), request.link(), request.tag());
    }

    @DeleteMapping("/tags")
    public void removeTag(@RequestBody LinkTagRequest request) {
        tagsService.removeTagFromLink(request.userId(), request.link(), request.tag());
    }
}
