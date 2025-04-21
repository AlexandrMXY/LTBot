package backend.academy.scrapper.controllers;

import backend.academy.api.model.NotificationPolicy;
import backend.academy.scrapper.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationsController {
    @Autowired
    private UserService userService;

    @PostMapping("/{user}/policy")
    public void setNotificationPolicy(@PathVariable long user, @RequestBody NotificationPolicy notificationPolicy) {
        userService.setNotificationPolicy(user, notificationPolicy);
    }

    @GetMapping("/{user}/policy")
    public NotificationPolicy getNotificationPolicy(@PathVariable long user) {
        return userService.getNotificationPolicy(user);
    }
}
