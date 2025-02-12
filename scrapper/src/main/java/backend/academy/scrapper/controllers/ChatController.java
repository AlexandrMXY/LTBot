package backend.academy.scrapper.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/th-chat/{id}")
public class ChatController {
    @PostMapping
    public void registerChat(@PathVariable("id") long id) {

    }

    @DeleteMapping
    public void deleteChat(@PathVariable("id") long id) {

    }
}
