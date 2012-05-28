import java.util.Calendar;
import java.util.GregorianCalendar;

public class StartTime {
    int timeGone;
    int startMseconds;
    int currentMseconds;
    int timeoutMseconds;

    StartTime(int timeoutInMseconds) {
        // work out current time in seconds and 
        Calendar cal = new GregorianCalendar();
        int sec = cal.get(Calendar.SECOND);  
        int min = cal.get(Calendar.MINUTE);           
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int milliSec = cal.get(Calendar.MILLISECOND);
        startMseconds = milliSec + (sec*1000) + (min *60000) + (hour*3600000);
        timeoutMseconds = (timeoutInMseconds);
    }

    int getTimeElapsed() {
        Calendar cal = new GregorianCalendar();
        int secElapsed = cal.get(Calendar.SECOND);
        int minElapsed = cal.get(Calendar.MINUTE);
        int hourElapsed = cal.get(Calendar.HOUR_OF_DAY);
        int milliSecElapsed = cal.get(Calendar.MILLISECOND);
        currentMseconds = milliSecElapsed + (secElapsed*1000) + (minElapsed *60000) + (hourElapsed * 3600000);
        timeGone = currentMseconds - startMseconds;
        return timeGone;
    }

    boolean timeout() {
        getTimeElapsed();
        if (timeGone >= timeoutMseconds) {
            return true;
        } else {
            return false;
        }
    }
}
