package backend.academy.scrapper.service;

import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public void createUser(long id) {
        userRepository.save(new User(id, List.of()));
    }
}
