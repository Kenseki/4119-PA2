import java.io.*;
import java.net.*;
import java.util.Calendar;

public class dvpublicMethods {
	
	public static double roundDouble(double d) {
		return (double) Math.round(d * 10000000) / 10000000;		
	}
	
	public static Boolean roundEqual(double d1, double d2) {
		return Math.abs(d1 - d2) < 0.0000005;
	}
	
	public static String timeString() {
		String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
		return timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3);
	}
	
	public static void sendMessage(DatagramSocket sock, String message, int port) {
		byte[] sendByte = new byte[10];
		sendByte = message.getBytes();
		DatagramPacket dp =null; 
		
		try {
			dp = new DatagramPacket(sendByte, sendByte.length, 
					InetAddress.getByName("127.0.0.1"), port);
			sock.send(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String recvMessage(DatagramSocket sock) {
		byte[] recvByte = new byte[1024];
		DatagramPacket dp = new DatagramPacket(recvByte, recvByte.length);
		
		try {
			sock.receive(dp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (dp.getAddress() == null) {
			return null;
		}
		return new String(recvByte, 0, dp.getLength());
	}
	
	
	public static int getFromPort(String message) {
		int index = message.indexOf('@');
		String fromPortString = message.substring(0, index);
		return Integer.parseInt(fromPortString);
	}
	
	public static String getUpdatePorts(String message) {
		int index = message.indexOf('@');
		return message.substring(index + 1);
	}
}
