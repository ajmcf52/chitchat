package misc;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


/**
 * this class is used to generate timestamps for the various 
 * entities sending message within the application.
 */
public class TimeStampGenerator {
    // the timezone id
    private static final ZoneId timezone = ZoneId.of(Constants.TIMEZONE);
    // timestamp formatter
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * method used to generate timestamps on demand for any given app entity.
     * @return timestamp in 24-hr clock format (00:00 to 23:59)
     */
    public String now() {
        String time = LocalTime.now(timezone).format(dtf);
        return time;
    }
    
}
