package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.updates.Updates;

public interface BotNotifierService {
    void sendUpdates(Updates updates);
}
