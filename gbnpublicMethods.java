import java.io.*;
import java.net.*;

public class gbnpublicMethods {
	
	public static void sendPacket(DatagramSocket socket, String packetString, int peerPort) {
		byte[] packetByte = new byte[5];
		packetByte = packetString.getBytes();
		
		DatagramPacket dp; 
		try {
			dp = new DatagramPacket(packetByte, packetByte.length, 
					InetAddress.getByName("127.0.0.1"), peerPort);
			socket.send(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String makePacketString(int seq, String data) {
		String seqString = String.valueOf(seq);
		while (seqString.length() < 4) 
			seqString = "0" + seqString;
		return seqString + data;
	}
	
	public static String receivePacket(DatagramSocket socket) {
		byte[] packetByte = new byte[1024];
		DatagramPacket dp = new DatagramPacket(packetByte, packetByte.length);
		
		try {
			socket.receive(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (dp.getAddress() == null) {
			return null;
		}
		return new String(packetByte, 0, dp.getLength());
	}
	
	public static int getSeq(String packetString) {
		String seqString = packetString.substring(0, 4);
		return Integer.parseInt(seqString);
	}
	
	public static String getData(String packetString) {
		if(packetString.length() > 4){
			return packetString.substring(4);
		}else{
			return "";
		}
	}
}
