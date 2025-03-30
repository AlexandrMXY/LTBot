package backend.academy.scrapper.dto.updates;

import java.util.ArrayList;
import java.util.List;
import lombok.ToString;

@ToString
public class Updates {
    private final List<Update> updateDetails = new ArrayList<>();

    public void addUpdate(Update details) {
        updateDetails.add(details);
    }

    public Updates addUpdates(List<Update> updates) {
        updateDetails.addAll(updates);
        return this;
    }

    public Updates mergeResult(Updates result) {
        updateDetails.addAll(result.updateDetails);
        return this;
    }

    public List<Update> getUpdates() {
        return updateDetails;
    }
}
