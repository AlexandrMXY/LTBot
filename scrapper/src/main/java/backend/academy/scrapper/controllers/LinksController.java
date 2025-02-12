package backend.academy.scrapper.controllers;

import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.LinkResponse;
import backend.academy.api.model.RemoveLinkRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/links")
public class LinksController {
    private static final Logger LOGGER = LogManager.getLogger();

    @GetMapping
    public void getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {

    }

    @PostMapping
    public LinkResponse addLinks(@RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLinkRequest request) {
        LOGGER.info("addLinks: {} {}", chatId, request);
        return new LinkResponse(chatId, request.url(), request.tags(), request.filters());
    }

    @DeleteMapping
    public void deleteLinks(@RequestHeader("Tg-Chat-Id") long chatId, RemoveLinkRequest request) {

    }
}
