package backend.academy.scrapper.controllers;

import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.LinkResponse;
import backend.academy.api.model.ListLinksResponse;
import backend.academy.api.model.RemoveLinkRequest;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.service.LinksManagementService;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class LinksController {

    @Autowired
    private LinksManagementService linksService;

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
}
