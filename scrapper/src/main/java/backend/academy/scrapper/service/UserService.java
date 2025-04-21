package backend.academy.scrapper.service;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.api.model.NotificationPolicy;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.util.TimeUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void setNotificationPolicy(long userId, NotificationPolicy notificationPolicy) {
        User u = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        User.NotificationStrategy strategy = User.NotificationStrategy.fromString(notificationPolicy.strategy());
        if (strategy == User.NotificationStrategy.INSTANT) {
            u.notificationTime(-1);
            u.notificationStrategy(strategy);
        } else {
            if (notificationPolicy.time() == null
                    || notificationPolicy.time() < 0
                    || u.notificationTime() >= TimeUtils.MINUTES_IN_DAY) {
                throw new IllegalArgumentException("Invalid notification policy time: " + notificationPolicy.time());
            }
            u.notificationTime(notificationPolicy.time());
            u.notificationStrategy(strategy);
        }
        userRepository.save(u);
    }

    public NotificationPolicy getNotificationPolicy(long userId) {
        User u = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        return new NotificationPolicy(
                u.notificationStrategy().toString(),
                u.notificationStrategy() == User.NotificationStrategy.INSTANT ? null : u.notificationTime());
    }
}
