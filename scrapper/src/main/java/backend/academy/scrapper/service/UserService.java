package backend.academy.scrapper.service;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void registerUserOrThrow(long id, RuntimeException exception) {
        if (userRepository.existsById(id)) {
            throw exception;
        }
        userRepository.save(new User(id, List.of()));
    }

    public void deleteUserOrThrow(long id, RuntimeException exception) {
        if (!userRepository.existsById(id)) {
            throw exception;
        }
        userRepository.deleteById(id);
    }
}
