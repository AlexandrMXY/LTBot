package backend.academy.scrapper.controllers;

import backend.academy.scrapper.service.UserService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat/{id}")
@TimeLimiter(name = ChatController.RESILIENCE4J_INSTANCE_NAME)
@RateLimiter(name = ChatController.RESILIENCE4J_INSTANCE_NAME)
public class ChatController {
    public static final String RESILIENCE4J_INSTANCE_NAME = "chat-controller";

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> registerChat(@PathVariable("id") long id) {
        userService.registerUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteChat(@PathVariable("id") long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
