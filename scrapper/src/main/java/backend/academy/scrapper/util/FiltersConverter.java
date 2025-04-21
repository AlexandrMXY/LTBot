package backend.academy.scrapper.util;

import backend.academy.scrapper.entities.filters.Filter;
import backend.academy.scrapper.entities.filters.Filters;
import backend.academy.scrapper.entities.filters.NameFilter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class FiltersConverter implements AttributeConverter<Filters, String> {
    @Override
    public String convertToDatabaseColumn(Filters attribute) {
        if (attribute == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Filter filter : attribute.filters()) {
            if (!builder.isEmpty()) {
                builder.append(";");
            }
            builder.append(filterToString(filter));
        }
        return builder.toString();
    }

    @Override
    public Filters convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        String[] filtersStrings = dbData.split(";");
        Filters filters = new Filters();
        for (String filter : filtersStrings) {
            filters.addFilter(parseFilter(filter));
        }
        return filters;
    }

    public static String filterToString(Filter filter) {
        return switch (filter) {
            case NameFilter nameFilter -> "name=" + nameFilter.name();
        };
    }

    public static Filter parseFilter(String filterString) {
        if (filterString.isEmpty()) return null;
        String[] splited = filterString.split("=");
        if (splited.length != 2) {
            throw new IllegalArgumentException("Invalid filter string: " + filterString);
        }
        return switch (splited[0]) {
            case "name" -> new NameFilter(splited[1]);
            default -> throw new IllegalArgumentException("Unsupported filter type: " + splited[0]);
        };
    }

    public static Filters parseFilters(List<String> filtersStrings) {
        Filters filters = new Filters();
        for (String filterString : filtersStrings) {
            filters.addFilter(parseFilter(filterString));
        }
        return filters;
    }

    public static List<String> filtersToStrings(Filters filters) {
        return filters.filters().stream().map(FiltersConverter::filterToString).toList();
    }
}
