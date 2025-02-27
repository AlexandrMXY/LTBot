package backend.academy.bot.utils;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RegExUtil {
    public static boolean isStringSatisfyRegex(String string, String regex) {
        return Pattern.compile(regex).matcher(string).matches();
    }
}
