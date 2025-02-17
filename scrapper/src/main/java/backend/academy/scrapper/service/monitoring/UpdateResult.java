package backend.academy.scrapper.service.monitoring;

import lombok.Getter;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class UpdateResult {
    private final List<UpdateDetails> updateDetails = new ArrayList<>();

    public void addUpdate(UpdateDetails details) {
        updateDetails.add(details);
    }

    public UpdateResult mergeResult(UpdateResult result) {
        updateDetails.addAll(result.updateDetails);
        return this;
    }



    public static record UpdateDetails(
        long user,
        String url,
        String message
    ) { }
}
