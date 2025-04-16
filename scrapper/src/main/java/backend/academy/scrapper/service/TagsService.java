package backend.academy.scrapper.service;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagsService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private LinkRepository linkRepository;

    @Transactional
    public void deactivateTag(long userId, String tag) {
        User user = repository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        if (!user.inactiveTags().contains(tag)) {
            user.inactiveTags().add(tag);
        }
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

    public List<LinkDto> getLinksWithTag(long userId, String tag) {
        if (!repository.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " not found");
        }
        return linkRepository.findAllByUserId(userId).stream()
                .filter(link -> link.tags().contains(tag))
                .map(LinkDto::new)
                .toList();
    }

    @Transactional
    public void addTagToLink(long userId, String url, String tag) {
        Optional<TrackedLink> linkOpt = linkRepository.findByUserIdAndUrl(userId, url);
        if (linkOpt.isEmpty()) {
            throw new NotFoundException("Link with user id " + userId + " and url " + url + " not found");
        }
        TrackedLink link = linkOpt.orElseThrow();
        if (link.tags().contains(tag)) {
            throw new AlreadyExistsException("Tag " + tag + " already exists in link " + url);
        }
        link.tags().add(tag);
        linkRepository.save(link);
    }

    @Transactional
    public void removeTagFromLink(long userId, String url, String tag) {
        Optional<TrackedLink> linkOpt = linkRepository.findByUserIdAndUrl(userId, url);
        if (linkOpt.isEmpty()) {
            throw new NotFoundException("Link with user id " + userId + "and url " + url + " not found");
        }
        TrackedLink link = linkOpt.orElseThrow();
        if (!link.tags().contains(tag)) {
            throw new NotFoundException("Tag " + tag + " does not exist in link " + url);
        }
        link.tags().remove(tag);
        linkRepository.save(link);
    }
}
