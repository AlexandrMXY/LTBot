package backend.academy.scrapper.util;

import lombok.experimental.UtilityClass;
import java.time.LocalDateTime;

@UtilityClass
public class TimeUtils {
    public static final int MINUTES_IN_DAY = 60 * 24;
    public static int getMinuteOfDay() {
        var now = LocalDateTime.now();
        return now.getHour() * 60 + now.getMinute();
    }
}
