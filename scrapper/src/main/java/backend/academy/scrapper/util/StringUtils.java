package backend.academy.scrapper.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {
    public static String clamp(String str, int maxLen) {
        if (str == null) return null;
        if (str.length() > maxLen) return str.substring(0, maxLen);
        return str;
    }
}
