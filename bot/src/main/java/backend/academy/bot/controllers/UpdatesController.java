package backend.academy.bot.controllers;

import backend.academy.api.model.LinkUpdate;
import backend.academy.bot.service.telegram.TelegramService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdatesController {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private TelegramService telegramService;

    @PostMapping("/updates")
    public ResponseEntity<?> updates(@RequestBody LinkUpdate update) {
        LOGGER.info(update);

        for (long id : update.tgChatIds()) {
            telegramService.sendMessage(id, "New update: " + update.url() + " " + update.description());
        }
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }
}
