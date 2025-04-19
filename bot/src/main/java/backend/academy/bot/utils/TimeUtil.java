package backend.academy.bot.utils;

import lombok.experimental.UtilityClass;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

@UtilityClass
public class TimeUtil {
    public String getTimeFromMinuteOfDayOrErrorString(Integer minute) {
        if (minute == null || minute < 0 || minute >= 60 * 24)
            return "Error";
        return (minute / 60) + ":" + (minute % 60);
    }

    public int getTimeOfDayFromString(String timeString) {
        try {
            LocalTime localTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"));
            return localTime.get(ChronoField.MINUTE_OF_DAY);
        } catch (Exception e) {
            return -1;
        }
    }
}
