import java.io.*;
import java.net.*;
import java.util.*;

public class gbnnode {
	public static int selfPort;
	public static int peerPort;
	public static int winSize;
	public static int winStart;
	public static int bufferSize;
	public static char[] buffer;
	public static String dorp;
	public static String norp;
	public static int nextseqnum;
	public static DatagramSocket gbnSock;
	
	public static void main(String args[]){
		
		if(args.length != 5){
			System.out.println("Usage: java gbnnode <self-port> <peer-port> <window-size> [ -d <value-of-n> j -p <value-of-p>]");
			return;
		}
		
		selfPort = Integer.parseInt(args[0]);
		peerPort = Integer.parseInt(args[1]);
		winSize = Integer.parseInt(args[2]);
	    winStart = 0;
		bufferSize = 2*winSize;
		buffer = new char[bufferSize];
		dorp = args[3];
		norp = args[4];
		if(dorp.equals("-d")){
			
		}else if(dorp.equals("-p")){
			
		}else{
			System.out.println("Usage: java gbnnode <self-port> <peer-port> <window-size> [ -d <value-of-n> j -p <value-of-p>]");
		    return;
		}

		try {
			gbnSock = new DatagramSocket(selfPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		gbnSender sender = new gbnSender();
		sender.start();
		gbnReceiver receiver = new gbnReceiver();
		receiver.start();
			
		
	}

}
