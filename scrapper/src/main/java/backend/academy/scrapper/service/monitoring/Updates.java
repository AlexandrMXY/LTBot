package backend.academy.scrapper.service.monitoring;

import lombok.Getter;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;


@ToString
public class Updates {
    private final List<Update> updateDetails = new ArrayList<>();

    public void addUpdate(Update details) {
        updateDetails.add(details);
    }

    public Updates mergeResult(Updates result) {
        updateDetails.addAll(result.updateDetails);
        return this;
    }

    public List<Update> getUpdates() {
        return updateDetails;
    }

    public boolean hasUpdates() {
        return !updateDetails.isEmpty();
    }

    public static record Update(
        List<Long> users,
        String url,
        String message
    ) { }
}
