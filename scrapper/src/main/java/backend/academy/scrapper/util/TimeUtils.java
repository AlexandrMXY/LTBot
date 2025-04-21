package backend.academy.scrapper.util;

import java.time.LocalDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TimeUtils {
    public static final int MINUTES_IN_DAY = 60 * 24;

    public static int getMinuteOfDay() {
        var now = LocalDateTime.now();
        return now.getHour() * 60 + now.getMinute();
    }
}
