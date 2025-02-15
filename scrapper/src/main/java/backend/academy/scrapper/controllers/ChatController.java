package backend.academy.scrapper.controllers;

import backend.academy.api.exceptions.InvalidRequestException;
import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.service.UserService;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class ChatController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> registerChat(@PathVariable("id") long id) {
        log.info("User registration request: id {}", id);
        userService.registerUserOrThrow(id, new InvalidRequestException("User already exists"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteChat(@PathVariable("id") long id) {
        log.info("User deletion request: id {}", id);
        userService.deleteUserOrThrow(id, new NotFoundException("User not found"));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
