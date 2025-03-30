package backend.academy.scrapper.service;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagsService {
    @Autowired
    UserRepository repository;

    @Transactional
    public void deactivateTag(long userId, String tag) {
        User user = repository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        if (!user.inactiveTags().contains(tag)) user.inactiveTags().add(tag);
        repository.save(user);
    }

    @Transactional
    public void reactivateTag(long userId, String tag) {
        User user = repository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        user.inactiveTags().remove(tag);
        repository.save(user);
    }

    public List<String> getDeactivatedTagsList(long userId) {
        return repository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"))
                .inactiveTags();
    }
}
