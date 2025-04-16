package backend.academy.bot.telegram.formatters;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.api.model.LinkUpdate;
import org.junit.jupiter.api.Test;

class UpdateFormatterTest {
    @Test
    public void format_answerType_shouldReturnCorrectString() {
        var result = new UpdateFormatter()
                .format(new LinkUpdate(
                        44, 1111L, "https://qertry.com", "CONTENT HERE", "User1", LinkUpdate.Types.ANSWER));

        assertEquals(
                "New answer from user User1 at https://qertry.com at 1970-01-01T00:18:31Z" + System.lineSeparator()
                        + "CONTENT HERE",
                result);
    }

    @Test
    public void format_issueType_shouldReturnCorrectString() {
        var result = new UpdateFormatter()
                .format(new LinkUpdate(
                        44, 1111L, "https://qertry.com", "CONTENT HERE", "User1", LinkUpdate.Types.ISSUE));

        assertEquals(
                "New issue from user User1 at https://qertry.com at 1970-01-01T00:18:31Z" + System.lineSeparator()
                        + "CONTENT HERE",
                result);
    }

    @Test
    public void format_commentType_shouldReturnCorrectString() {
        var result = new UpdateFormatter()
                .format(new LinkUpdate(
                        44, 1111L, "https://qertry.com", "CONTENT HERE", "User1", LinkUpdate.Types.COMMENT));

        assertEquals(
                "New comment from user User1 at https://qertry.com at 1970-01-01T00:18:31Z" + System.lineSeparator()
                        + "CONTENT HERE",
                result);
    }

    @Test
    public void format_pullRequestType_shouldReturnCorrectString() {
        var result = new UpdateFormatter()
                .format(new LinkUpdate(
                        44, 1111L, "https://qertry.com", "CONTENT HERE", "User1", LinkUpdate.Types.PULL_REQUEST));

        assertEquals(
                "New pull request from user User1 at https://qertry.com at 1970-01-01T00:18:31Z"
                        + System.lineSeparator() + "CONTENT HERE",
                result);
    }

    @Test
    public void format_unknownType_shouldReturnCorrectString() {
        var result = new UpdateFormatter()
                .format(new LinkUpdate(
                        44, 1111L, "https://qertry.com", "CONTENT HERE", "User1", "dqvjwnbjklmqklfanvbjinjkwgnljok"));

        assertEquals(
                "New update from user User1 at https://qertry.com at 1970-01-01T00:18:31Z" + System.lineSeparator()
                        + "CONTENT HERE",
                result);
    }

    @Test
    public void format_nullType_shouldReturnCorrectString() {
        var result = new UpdateFormatter()
                .format(new LinkUpdate(44, 1111L, "https://qertry.com", "CONTENT HERE", "User1", null));

        assertEquals(
                "New update from user User1 at https://qertry.com at 1970-01-01T00:18:31Z" + System.lineSeparator()
                        + "CONTENT HERE",
                result);
    }
}
