package backend.academy.scrapper.dto.updates;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@ToString
@NoArgsConstructor
public class Updates implements Iterable<Update> {
    private List<Update> updates = new ArrayList<>();

    public Updates(List<Update> updates) {
        this.updates = updates;
    }

    public void addUpdate(Update details) {
        updates.add(details);
    }

    public Updates addUpdates(List<Update> updates) {
        this.updates.addAll(updates);
        return this;
    }

    public Updates mergeResult(Updates result) {
        updates.addAll(result.updates);
        return this;
    }

    public Updates extractDelayed() {
        Updates delayed = new Updates();
        List<Update> notDelayed = new ArrayList<>();
        for (Update update : updates) {
            if (update.delayedNotification()) {
                delayed.addUpdate(update);
            } else {
                notDelayed.add(update);
            }
        }
        this.updates = notDelayed;
        return delayed;
    }

    public List<Update> getUpdates() {
        return updates;
    }

    @Override
    public @NotNull Iterator<Update> iterator() {
        return updates.iterator();
    }
}
