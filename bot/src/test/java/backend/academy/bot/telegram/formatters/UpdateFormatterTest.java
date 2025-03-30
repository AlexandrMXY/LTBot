package backend.academy.bot.telegram.formatters;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.api.model.LinkUpdate;
import org.junit.jupiter.api.Test;

class UpdateFormatterTest {
    @Test
    public void format_whenCalled_shouldReturnCorrectString() {
        var result =
                new UpdateFormatter().format(new LinkUpdate(44, 1111L, "https://qertry.com", "CONTENT HERE", "User1"));

        assertEquals(
                "New update at https://qertry.com from user User1 at 1970-01-01T00:18:31Z" + System.lineSeparator()
                        + "CONTENT HERE",
                result);
    }
}
