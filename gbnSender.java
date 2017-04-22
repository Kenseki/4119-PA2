import java.io.*;
import java.util.*;

public class gbnSender extends Thread{
    public static int bufferEnd;
	public static Timer timer;
    
	public gbnSender(){
        timer = new Timer();
    }
        
    public void run(){
    	Scanner in = new Scanner(System.in);
    	for(;;){

    		System.out.print("node> ");
    		if(!in.hasNextLine()){
    			System.out.println("Error: invalid command");
    			continue;
    		}
    		String command = in.nextLine();
    		if(command.equals("\n")){
    			continue;
    		}
    		int i = command.indexOf(" ");
    		if(i<0){
    			System.out.println("Error: invalid command");
    			continue;
    		}
    		String operation = command.substring(0, i);
    		String message = command.substring(i+1);
    		if(!operation.equals("send")){
    			System.out.println("Error: invalid command");
    			continue;
    		}
    		
    		sendToBuffer(message);
    		try {
				Thread.sleep(73);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		System.out.print("[Summary] "+gbnReceiver.ackDropCounter+"/"+gbnReceiver.ackTotalCounter+" packets discarded, loss rate = ");
    		System.out.printf("%.3f\n", gbnReceiver.ackDropCounter/((double)gbnReceiver.ackTotalCounter));
    		gbnpublicMethods.sendPacket(gbnnode.gbnSock, "End", gbnnode.peerPort);
    	}
    }
    
    private void sendToBuffer(String message){
    	for(;;){
    		if(message.length()>gbnnode.bufferSize){
    			for(int i = 0; i< gbnnode.bufferSize; i++){
    				gbnnode.buffer[(i+gbnnode.winStart)%gbnnode.bufferSize] = message.charAt(i);
    			}
    			sendGBN(gbnnode.bufferSize+gbnnode.winStart);
    			message = message.substring(gbnnode.bufferSize);
    		}else{
    			for(int i = 0; i< message.length(); i++){
    				gbnnode.buffer[(i+gbnnode.winStart)%gbnnode.bufferSize] = message.charAt(i);
    			}
    			sendGBN(message.length()+gbnnode.winStart);
    			break;
    		}
    	}
    }
    
    private void sendGBN(int bufferEnd){
    	gbnSender.bufferEnd = bufferEnd;
    	gbnnode.nextseqnum = gbnnode.winStart;
    	timer.schedule(new newTimerTask(), 500, 500);
    	
    	while(gbnnode.winStart<bufferEnd){
    		if(gbnnode.nextseqnum<(gbnnode.winStart+gbnnode.winSize) && gbnnode.nextseqnum<bufferEnd){
    			String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
    		    System.out.println("["+timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3)+"] packet"+gbnnode.nextseqnum+" "+String.valueOf(gbnnode.buffer[gbnnode.nextseqnum%gbnnode.bufferSize])+ " sent");
    		    try {
					Thread.sleep(0);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		    String packetString = gbnpublicMethods.makePacketString(gbnnode.nextseqnum, String.valueOf(gbnnode.buffer[gbnnode.nextseqnum%gbnnode.bufferSize]));
    		    gbnpublicMethods.sendPacket(gbnnode.gbnSock, packetString, gbnnode.peerPort);
    		    gbnnode.nextseqnum++;
    		}else{
    			try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    	timer.cancel();
    	timer = new Timer();
    }
        
}
