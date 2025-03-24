package backend.academy.scrapper.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class StringListConverterTest {
    @Test
    void convertToDatabaseColumn_correctInput_correctOutput() {
        assertEquals("qwerty;qwerty1", new StringListConverter().convertToDatabaseColumn(List.of("qwerty", "qwerty1")));
    }

    @Test
    void convertToDatabaseColumn_emptyInput_emptyOutput() {
        assertEquals("", new StringListConverter().convertToDatabaseColumn(List.of()));
    }

    @Test
    void convertToDatabaseColumn_nullInput_nullOutput() {
        assertNull(new StringListConverter().convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_inputContainsSeparator_throw() {
        assertThrows(IllegalArgumentException.class, () -> new StringListConverter()
                .convertToDatabaseColumn(List.of("qw;erty", "qwerty1")));
    }

    @Test
    void convertToEntityAttribute_correctInput_correctOutput() {
        assertIterableEquals(
                List.of("qwerty", "qwerty1"), new StringListConverter().convertToEntityAttribute("qwerty;qwerty1"));
    }

    @Test
    void convertToEntityAttribute_emptyInput_emptyOutput() {
        assertIterableEquals(List.of(), new StringListConverter().convertToEntityAttribute(""));
    }

    @Test
    void convertToEntityAttribute_nullInput_nullOutput() {
        assertNull(new StringListConverter().convertToEntityAttribute(null));
    }
}
