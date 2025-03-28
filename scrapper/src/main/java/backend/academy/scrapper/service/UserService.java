package backend.academy.scrapper.service;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void registerUser(long id) {
        if (userRepository.existsById(id)) {
            throw new AlreadyExistsException();
        }
        userRepository.save(new User(id, List.of()));
    }

    public void deleteUser(long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException();
        }
        userRepository.deleteById(id);
    }
}
