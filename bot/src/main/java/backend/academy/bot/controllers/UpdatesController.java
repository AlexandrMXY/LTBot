package backend.academy.bot.controllers;

import backend.academy.api.model.LinkUpdate;
import backend.academy.bot.service.telegram.TelegramService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class UpdatesController {

    @Autowired
    private TelegramService telegramService;

    @PostMapping("/updates")
    public ResponseEntity<?> updates(@RequestBody LinkUpdate update) {
        for (long id : update.tgChatIds()) {
            telegramService.sendMessage(id, "New update: " + update.url() + " " + update.description());
        }
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }
}
