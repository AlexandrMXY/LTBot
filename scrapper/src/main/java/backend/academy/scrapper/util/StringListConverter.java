package backend.academy.scrapper.util;

import jakarta.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringListConverter implements AttributeConverter<List<String>, String> {
    public static final String SEPARATOR = ";";
    @Override
    public String convertToDatabaseColumn(List<String> strings) {
        if (strings == null)
            return null;
        return String.join(SEPARATOR, strings);
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        if (string == null)
            return new ArrayList<>();
        return Arrays.stream(string.split(SEPARATOR)).toList();
    }
}
