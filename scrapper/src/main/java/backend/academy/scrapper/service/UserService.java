package backend.academy.scrapper.service;

import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
