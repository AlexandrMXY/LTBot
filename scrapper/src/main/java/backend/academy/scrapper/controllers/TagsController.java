package backend.academy.scrapper.controllers;

import backend.academy.api.model.TagsListResponse;
import backend.academy.api.model.TagsRequest;
import backend.academy.scrapper.service.TagsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TagsController {
    @Autowired
    private TagsService tagsService;

    @PostMapping("/tags/deactivate")
    public ResponseEntity<Void> deactivate(@RequestBody TagsRequest request) {
        tagsService.deactivateTag(request.userId(), request.tag());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/reactivate")
    public ResponseEntity<Void> reactivate(@RequestBody TagsRequest request) {
        tagsService.reactivateTag(request.userId(), request.tag());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tags/{id}")
    public TagsListResponse getDeactivatedTagsList(@PathVariable long id) {
        return new TagsListResponse(id, tagsService.getDeactivatedTagsList(id));
    }
}
