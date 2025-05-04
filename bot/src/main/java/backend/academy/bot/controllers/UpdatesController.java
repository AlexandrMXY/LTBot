package backend.academy.bot.controllers;

import backend.academy.api.model.LinkUpdate;
import backend.academy.bot.service.UpdatesService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@TimeLimiter(name = UpdatesController.RESILIENCE4J_INSTANCE_NAME)
@RateLimiter(name = UpdatesController.RESILIENCE4J_INSTANCE_NAME)
public class UpdatesController {
    public static final String RESILIENCE4J_INSTANCE_NAME = "updates-controller";

    @Autowired
    private UpdatesService updatesService;

    @PostMapping("/updates")
    public ResponseEntity<?> updates(@RequestBody LinkUpdate update) {
        updatesService.processUpdate(update);
        return new ResponseEntity<>(HttpStatusCode.valueOf(200));
    }
}
