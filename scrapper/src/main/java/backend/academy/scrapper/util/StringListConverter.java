package backend.academy.scrapper.util;

import jakarta.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringListConverter implements AttributeConverter<List<String>, String> {
    public static final String SEPARATOR = ";";

    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        if (strings == null) return null;
        if (strings.stream().anyMatch((s) -> s.contains(SEPARATOR))) {
            throw new IllegalArgumentException(
                    "Unable to covert string which contains separator (" + SEPARATOR + ") as a character");
        }
        return String.join(SEPARATOR, strings);
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        if (string == null) return null;
        if (string.isEmpty()) return new ArrayList<>();
        return Arrays.stream(string.split(SEPARATOR)).collect(Collectors.toCollection(ArrayList::new));
    }
}
