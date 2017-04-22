import java.util.*;

public class cnGBN {
	public int peerPort;
	public int winSize;
	public int winStart;
	public int nextseqnum;
	public int sentNum;
	public int succNum;
	public double currLossRate;
	Timer timer;
	
	public class TimeoutTask extends TimerTask {
		public void run() {
			nextseqnum = winStart;
		}

	}
	
	public cnGBN(int peerPort, int winSize) {
		this.peerPort = peerPort;
		this.winSize = winSize;
		
		nextseqnum = 0;
		winStart = 0;
		sentNum = 0;
		succNum = 0;
		currLossRate = 0;
	}
	
	public int lossNum() {
		return sentNum - succNum;
	}
	
	
	public double lossRate() {
		if (sentNum == 0) {
			return 0;
		}
		return cnpublicMethods.roundDouble((sentNum - succNum) / (double) sentNum);

	}
	
	public void startTimer() {
		timer = new Timer();
		timer.schedule(new TimeoutTask(), 500, 500);
	}
	
	public void sendProb() {
		if (nextseqnum>=(winStart+winSize)){
			return;
		}
		String message = String.valueOf(cnnode.selfPort)+"@"+ String.valueOf(nextseqnum++);
		message = cnpublicMethods.addType(message, cnnode.MessageType.PROBE);
		cnpublicMethods.sendMessage(cnnode.cnSock, message, peerPort);
		sentNum++;
	}
	
	public void cancelTimer() {
		timer.cancel();
	}
	
	
	public void updateLossRate() {
		if (sentNum == 0) {
			currLossRate = 0;
			return;
		}
		currLossRate = cnpublicMethods.roundDouble((sentNum-succNum)/((double) sentNum));
	}

}
