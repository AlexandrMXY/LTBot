package backend.academy.bot.utils;

import lombok.experimental.UtilityClass;
import java.util.regex.Pattern;

@UtilityClass
public class RegExUtil {
    public static boolean isStringSatisfyRegex(String string, String regex) {
        return Pattern.compile(regex).matcher(string).matches();
    }
}
