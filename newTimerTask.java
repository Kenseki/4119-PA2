import java.util.*;

public class newTimerTask extends TimerTask{
    public void run(){
    	String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
    	System.out.println("[" + timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+ "]" + " packet" + gbnnode.winStart + " timeout");
        gbnnode.nextseqnum = gbnnode.winStart;
    }
}
