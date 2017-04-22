import java.util.*;


public class gbnReceiver extends Thread{
	public static int ackDropCounter = 0;
	public static int ackTotalCounter = 0;
	public static int pktDropCounter = 0;
	public static int pktTotalCounter = 0;
	Random rand = new Random(System.currentTimeMillis());
	int count =0;
	
	public void run(){
		int ack = 0;
		for(;;){
			String packetString = gbnpublicMethods.receivePacket(gbnnode.gbnSock);
			if(packetString.equals("End")){
	    		System.out.print("[Summary] "+gbnReceiver.pktDropCounter+"/"+gbnReceiver.pktTotalCounter+" packets discarded, loss rate = ");
	    		System.out.printf("%.3f\n", gbnReceiver.pktDropCounter/((double)gbnReceiver.pktTotalCounter));
	    		System.out.print("node> ");
			    continue;
			}
			int seq = gbnpublicMethods.getSeq(packetString);
			String data = gbnpublicMethods.getData(packetString);
			
			if(data.length() ==0){
				ackTotalCounter++;
				if(isDrop(gbnnode.dorp)){
					ackDropCounter++;
					String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
					System.out.println("["+timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+"]"+" ACK" + String.valueOf(seq - 1) + " discarded");
					continue;
				}
				if (seq > gbnnode.winStart) {
					gbnnode.winStart = seq;
					String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
					System.out.println("[" +timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+ "]" +" ACK" + String.valueOf(seq - 1) + " received, window moves to "+seq);
					if (seq < gbnSender.bufferEnd) {
						gbnSender.timer.cancel();
						gbnSender.timer = new Timer();
						gbnSender.timer.schedule(new newTimerTask(), 500, 500);
					}
				} else {
					String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
					System.out.println("[" +timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+ "]" +" ACK"+String.valueOf(seq - 1)+" received");
				}
			}else{
				pktTotalCounter++;
				if(isDrop(gbnnode.dorp)){
					pktDropCounter++;
					String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
					System.out.println("["+timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+"]"+" packet" + seq+" "+ data + " discarded");
					continue;
				}
				if(seq == ack){
					String ackString = gbnpublicMethods.makePacketString(++ack, "");
					gbnpublicMethods.sendPacket(gbnnode.gbnSock, ackString, gbnnode.peerPort);
					
					String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
					System.out.println("["+timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+"]"+" packet"+seq+" "+data+ " received");
					
					timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
					System.out.println("[" +timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+ "]"+" ACK"+String.valueOf(ack - 1)+" sent, expecting packet"+ack);
				}else{
					String ackString = gbnpublicMethods.makePacketString(ack, "");
					gbnpublicMethods.sendPacket(gbnnode.gbnSock, ackString, gbnnode.peerPort);
					
					String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
					System.out.println("["+timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+"]"+" packet"+seq+" "+data+ " received");
					
					timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
					System.out.println("[" +timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+ "]"+" ACK"+String.valueOf(ack - 1)+" sent, expecting packet"+ack);
				}
				
			}
		}
	}
	
	private boolean isDrop(String dorp){
		if(dorp.equals("-p")){
			return (rand.nextDouble() <= Double.parseDouble(gbnnode.norp));
		}else{
			count++;
			if((count % Integer.parseInt(gbnnode.norp)) ==0){
				count =0;
				return true;
			}else{
				return false;
			}
			
		}
	}
}
